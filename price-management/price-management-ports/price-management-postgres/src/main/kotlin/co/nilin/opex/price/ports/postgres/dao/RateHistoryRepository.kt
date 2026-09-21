package co.nilin.opex.price.ports.postgres.dao

import co.nilin.opex.price.ports.postgres.model.CrossRateSparkPoint
import co.nilin.opex.price.ports.postgres.model.RateHistoryModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface RateHistoryRepository : ReactiveCrudRepository<RateHistoryModel, Long> {

    @Query("SELECT * FROM rate_history WHERE symbol = :symbol ORDER BY created_date DESC LIMIT :limit")
    fun findBySymbolWithLimit(symbol: String, limit: Int): Flux<RateHistoryModel>

    @Query("SELECT * FROM rate_history WHERE symbol = :symbol ORDER BY created_date DESC LIMIT 1")
    fun findLatestBySymbol(symbol: String): Mono<RateHistoryModel>

    @Query("SELECT DISTINCT ON (symbol) * FROM rate_history ORDER BY symbol, created_date DESC")
    fun findLatestForAllSymbols(): Flux<RateHistoryModel>

    @Query(
        "SELECT * FROM rate_history WHERE symbol = :symbol AND created_date BETWEEN :startTime AND :endTime " +
            "ORDER BY created_date ASC"
    )
    fun findBySymbolAndCreatedDateBetween(
        symbol: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): Flux<RateHistoryModel>

    @Query(
        "SELECT * FROM rate_history WHERE created_date BETWEEN :startTime AND :endTime ORDER BY symbol, created_date ASC"
    )
    fun findAllByCreatedDateBetween(startTime: LocalDateTime, endTime: LocalDateTime): Flux<RateHistoryModel>

    /**
     * Reconstructs a `<asset>-<refCurrency>` sparkline for every asset that appears in
     * `rate_history` either directly against [refCurrency], or against [hub] (except
     * [refCurrency] itself), sampled into [points] evenly spaced buckets over
     * [startTime, endTime]. A direct `<asset>-<refCurrency>` (or inverse) pair, when present,
     * always wins over a hub-bridged reconstruction for that asset - it's real observed data
     * rather than a derived estimate.
     *
     * Each candidate is normalized into one of two "sources": `direct` (already priced in ref)
     * or `hub` (priced in hub, later divided by the ref's own hub price so
     * `<asset>-<refCurrency> = (asset in hub) / (ref in hub)`). Both sources share the same
     * bucket/carry-forward machinery, partitioned by (asset, source).
     *
     * Pipeline (all computed in this one query):
     *   rate_history -> normalize (asset, source, t, px) for direct-ref and hub pairs
     *   -> generate N buckets -> union buckets + ticks per (asset, source)
     *   -> window carry-forward (count-based gaps-and-islands), carry-backward via earliest tick
     *   -> keep bucket rows -> direct rows pass through, hub rows self-join against ref on bucket
     *      time and divide, direct-sourced assets take precedence over hub-derived ones
     *   -> per-symbol first/last -> change % and trend.
     */
    @Query(
        """
        WITH p AS (
            SELECT upper(trim(:refCurrency)) AS ref,
                   upper(trim(:hub))         AS hub,
                   CAST(:startTime AS timestamp) AS ts_start,
                   CAST(:endTime   AS timestamp) AS ts_end,
                   GREATEST(CAST(:points AS int), 2) AS n
        ),
        -- 1+2: normalize once. asset = the other side of the pair, px = asset priced in that source unit.
        normalized AS (
            -- hub-bridged: asset priced in hub
            SELECT CASE WHEN sp.q = p.hub THEN sp.b ELSE sp.q END AS asset,
                   'hub' AS source,
                   rh.created_date AS t,
                   CASE WHEN sp.q = p.hub THEN rh.price ELSE 1.0 / rh.price END AS px
            FROM rate_history rh
            CROSS JOIN p
            CROSS JOIN LATERAL (
                SELECT split_part(upper(rh.symbol), '-', 1) AS b,
                       split_part(upper(rh.symbol), '-', 2) AS q
            ) sp
            WHERE rh.price <> 0
              AND rh.created_date <= p.ts_end
              -- one extra period of look-back to seed the carry-forward at the first bucket
              AND rh.created_date >= p.ts_start - (p.ts_end - p.ts_start)
              AND (sp.b = p.hub OR sp.q = p.hub)

            UNION ALL

            -- direct: asset already priced in ref, no bridging needed - takes precedence later
            SELECT CASE WHEN sp.q = p.ref THEN sp.b ELSE sp.q END AS asset,
                   'direct' AS source,
                   rh.created_date AS t,
                   CASE WHEN sp.q = p.ref THEN rh.price ELSE 1.0 / rh.price END AS px
            FROM rate_history rh
            CROSS JOIN p
            CROSS JOIN LATERAL (
                SELECT split_part(upper(rh.symbol), '-', 1) AS b,
                       split_part(upper(rh.symbol), '-', 2) AS q
            ) sp
            WHERE rh.price <> 0
              AND rh.created_date <= p.ts_end
              AND rh.created_date >= p.ts_start - (p.ts_end - p.ts_start)
              AND (sp.b = p.ref OR sp.q = p.ref)
              AND p.ref <> p.hub -- ref==hub is already fully covered by the hub-bridged branch

            UNION ALL

            -- the hub itself: 1 unit of hub is worth 1 hub (so hub-<ref> is derivable by division)
            SELECT p.hub AS asset, 'hub' AS source, p.ts_start AS t, 1.0 AS px FROM p
        ),
        direct_assets AS (
            SELECT DISTINCT asset FROM normalized WHERE source = 'direct'
        ),
        asset_sources AS (
            SELECT DISTINCT asset, source FROM normalized
        ),
        -- 3: evenly spaced buckets
        buckets AS (
            SELECT p.ts_start
                   + (p.ts_end - p.ts_start) * (g.i::float8 / GREATEST(p.n - 1, 1)::float8) AS b
            FROM p, generate_series(0, p.n - 1) AS g(i)
        ),
        -- 4: one row per (asset, source, tick) and per (asset, source, bucket)
        events AS (
            SELECT as2.asset, as2.source, bk.b AS t, NULL::numeric AS px
            FROM asset_sources as2 CROSS JOIN buckets bk
            UNION ALL
            SELECT n.asset, n.source, n.t, n.px FROM normalized n
        ),
        -- 5: carry-forward. count() over an ordered window increments only on ticks, so rows
        -- between two ticks share a group; max(px) in that group is the carried value.
        carried AS (
            SELECT asset, source, t, px,
                   max(px) OVER (PARTITION BY asset, source, grp) AS cf
            FROM (
                SELECT asset, source, t, px,
                       count(px) OVER (PARTITION BY asset, source ORDER BY t) AS grp
                FROM events
            ) g
        ),
        earliest AS (
            SELECT DISTINCT ON (asset, source) asset, source, px AS first_price
            FROM normalized
            ORDER BY asset, source, t ASC
        ),
        -- 6: price for every (asset, source, bucket); carry-backward with the earliest known tick
        bucket_price AS (
            SELECT c.asset, c.source, c.t, COALESCE(c.cf, e.first_price) AS price
            FROM carried c
            JOIN earliest e ON e.asset = c.asset AND e.source = c.source
            WHERE c.px IS NULL
        ),
        ref_bucket AS (
            SELECT bp.t, bp.price
            FROM bucket_price bp CROSS JOIN p
            WHERE bp.asset = p.ref AND bp.source = 'hub'
        ),
        -- 7: direct rows pass through as-is; hub rows self-join against ref on bucket time and
        -- divide, but only for assets that have no direct data of their own.
        cross_rate AS (
            SELECT bp.asset || '-' || p.ref AS symbol,
                   bp.t AS bucket_time,
                   CASE WHEN p.ref = p.hub THEN bp.price
                        ELSE bp.price / NULLIF(rb.price, 0) END AS price
            FROM bucket_price bp
            CROSS JOIN p
            LEFT JOIN ref_bucket rb ON rb.t = bp.t
            WHERE bp.source = 'hub'
              AND bp.asset <> p.ref
              AND bp.asset NOT IN (SELECT asset FROM direct_assets)

            UNION ALL

            SELECT bp.asset || '-' || p.ref AS symbol,
                   bp.t AS bucket_time,
                   bp.price AS price
            FROM bucket_price bp
            CROSS JOIN p
            WHERE bp.source = 'direct'
        ),
        -- 8: per-symbol first/last -> change % and trend
        ranked AS (
            SELECT symbol, bucket_time, price,
                   count(*)          OVER (PARTITION BY symbol) AS cnt,
                   first_value(price) OVER w AS first_p,
                   last_value(price)  OVER w AS last_p
            FROM cross_rate
            WHERE price IS NOT NULL
            WINDOW w AS (PARTITION BY symbol ORDER BY bucket_time
                         ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)
        )
        SELECT symbol,
               bucket_time,
               price,
               CASE WHEN first_p = 0 THEN 0
                    ELSE round((last_p - first_p) / first_p * 100, 2) END AS change_percent,
               (last_p >= first_p) AS is_trend_up
        FROM ranked
        WHERE cnt >= 2
        ORDER BY symbol, bucket_time
        """
    )
    fun findCrossRateSparkPoints(
        refCurrency: String,
        hub: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        points: Int
    ): Flux<CrossRateSparkPoint>
}
