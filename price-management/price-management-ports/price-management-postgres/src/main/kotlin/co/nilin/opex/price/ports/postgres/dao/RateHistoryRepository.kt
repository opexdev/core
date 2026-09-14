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
     * `rate_history` paired with [hub] (except [refCurrency]), sampled into [points] evenly
     * spaced buckets over [startTime, endTime]. Each asset is first normalized to a price in
     * [hub] (direct pair `<asset>-<hub>`, or `1 / <hub>-<asset>`), then divided by the
     * reference currency's own hub price, so `<asset>-<refCurrency> = (asset in hub) / (ref in hub)`.
     *
     * Pipeline (all computed in this one query):
     *   rate_history -> normalize (asset, t, hub_price) for hub pairs only
     *   -> generate N buckets -> union buckets + ticks per asset
     *   -> window carry-forward (count-based gaps-and-islands), carry-backward via earliest tick
     *   -> keep bucket rows -> self-join asset vs. ref on bucket time -> divide
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
        -- 1+2: normalize once, hub pairs only. asset = the non-hub side, hub_price = asset priced in hub.
        normalized AS (
            SELECT CASE WHEN sp.q = p.hub THEN sp.b ELSE sp.q END AS asset,
                   rh.created_date AS t,
                   CASE WHEN sp.q = p.hub THEN rh.price ELSE 1.0 / rh.price END AS hub_price
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
        ),
        all_assets AS (
            SELECT asset FROM normalized
            UNION                       -- the hub itself: 1 unit of hub is worth 1 hub (so hub-<ref> is derivable)
            SELECT hub FROM p
        ),
        -- 3: evenly spaced buckets
        buckets AS (
            SELECT p.ts_start
                   + (p.ts_end - p.ts_start) * (g.i::float8 / GREATEST(p.n - 1, 1)::float8) AS b
            FROM p, generate_series(0, p.n - 1) AS g(i)
        ),
        -- 4: one row per (asset, tick) and per (asset, bucket)
        events AS (
            SELECT aa.asset, bk.b AS t, NULL::numeric AS hub_price
            FROM all_assets aa CROSS JOIN buckets bk
            UNION ALL
            SELECT n.asset, n.t, n.hub_price FROM normalized n
        ),
        -- 5: carry-forward. count() over an ordered window increments only on ticks, so rows
        -- between two ticks share a group; max(hub_price) in that group is the carried value.
        carried AS (
            SELECT asset, t, hub_price,
                   max(hub_price) OVER (PARTITION BY asset, grp) AS cf
            FROM (
                SELECT asset, t, hub_price,
                       count(hub_price) OVER (PARTITION BY asset ORDER BY t) AS grp
                FROM events
            ) g
        ),
        earliest AS (
            (SELECT DISTINCT ON (asset) asset, hub_price AS first_price
             FROM normalized
             ORDER BY asset, t ASC)
            UNION ALL
            SELECT hub AS asset, 1.0 AS first_price FROM p
        ),
        -- 6: hub price for every (asset, bucket); carry-backward with the earliest known tick
        bucket_price AS (
            SELECT c.asset, c.t, COALESCE(c.cf, e.first_price) AS price
            FROM carried c
            JOIN earliest e ON e.asset = c.asset
            WHERE c.hub_price IS NULL
        ),
        ref_bucket AS (
            SELECT bp.t, bp.price
            FROM bucket_price bp CROSS JOIN p
            WHERE bp.asset = p.ref
        ),
        -- 7: self-join asset vs. ref on bucket time, then divide
        cross_rate AS (
            SELECT bp.asset || '-' || p.ref AS symbol,
                   bp.t AS bucket_time,
                   CASE WHEN p.ref = p.hub THEN bp.price
                        ELSE bp.price / NULLIF(rb.price, 0) END AS price
            FROM bucket_price bp
            CROSS JOIN p
            LEFT JOIN ref_bucket rb ON rb.t = bp.t
            WHERE bp.asset <> p.ref
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
