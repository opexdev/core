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

    // Seek directly to the next symbol in the (symbol, created_date DESC) index.
    @Query(
        """
        WITH RECURSIVE latest AS (
            (SELECT * FROM rate_history ORDER BY symbol, created_date DESC LIMIT 1)
            UNION ALL
            SELECT next_rate.*
            FROM latest
            CROSS JOIN LATERAL (
                SELECT *
                FROM rate_history
                WHERE symbol > latest.symbol
                ORDER BY symbol, created_date DESC
                LIMIT 1
            ) next_rate
        )
        SELECT * FROM latest ORDER BY symbol
        """
    )
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
     * For each matching history symbol, index lookups supply the latest tick at or before
     * each bucket (carry-forward), or the earliest tick in the look-back window when none
     * precedes it. The query merges direct and inverse histories, then derives bridged rates
     * and calculates the first-to-last change.
     */
    @Query(
        """
        WITH RECURSIVE p AS (
            SELECT upper(trim(:refCurrency)) AS ref,
                   upper(trim(:hub))         AS hub,
                   CAST(:startTime AS timestamp) AS ts_start,
                   CAST(:endTime   AS timestamp) AS ts_end,
                   GREATEST(CAST(:points AS int), 2) AS n
        ),
        -- Use the index to skip the historical rows between distinct symbols.
        symbols(symbol) AS (
            (SELECT symbol FROM rate_history ORDER BY symbol LIMIT 1)
            UNION ALL
            SELECT next_symbol.symbol
            FROM symbols s
            CROSS JOIN LATERAL (
                SELECT symbol FROM rate_history
                WHERE symbol > s.symbol ORDER BY symbol LIMIT 1
            ) next_symbol
        ),
        candidates AS (
            SELECT s.symbol, u.source,
                   CASE WHEN sp.q = u.currency THEN sp.b ELSE sp.q END AS asset,
                   sp.q = u.currency AS is_forward,
                   p.ts_start - (p.ts_end - p.ts_start) AS seed_start,
                   p.ts_end
            FROM symbols s CROSS JOIN p
            CROSS JOIN LATERAL (
                VALUES (p.hub, 'hub'), (p.ref, 'direct')
            ) u(currency, source)
            CROSS JOIN LATERAL (
                SELECT split_part(upper(s.symbol), '-', 1) AS b,
                       split_part(upper(s.symbol), '-', 2) AS q
            ) sp
            WHERE (u.source = 'hub' OR p.ref <> p.hub)
              AND (sp.b = u.currency OR sp.q = u.currency)
        ),
        seeds AS MATERIALIZED (
            SELECT c.*, seed.created_date AS seed_time, seed.price AS seed_price
            FROM candidates c
            CROSS JOIN LATERAL (
                SELECT created_date, price
                FROM rate_history
                WHERE symbol = c.symbol AND price <> 0
                  AND created_date BETWEEN c.seed_start AND c.ts_end
                ORDER BY created_date
                LIMIT 1
            ) seed
        ),
        direct_assets AS (
            SELECT DISTINCT asset FROM seeds WHERE source = 'direct'
        ),
        buckets AS (
            SELECT p.ts_start
                   + (p.ts_end - p.ts_start) * (g.i::float8 / GREATEST(p.n - 1, 1)::float8) AS b
            FROM p, generate_series(0, p.n - 1) AS g(i)
        ),
        samples AS (
            SELECT s.asset, s.source, bk.b AS t,
                   COALESCE(tick.created_date, s.seed_time) AS tick_time,
                   CASE WHEN s.is_forward THEN COALESCE(tick.price, s.seed_price)
                        ELSE 1.0 / COALESCE(tick.price, s.seed_price) END AS price
            FROM seeds s
            CROSS JOIN buckets bk
            LEFT JOIN LATERAL (
                SELECT created_date, price
                FROM rate_history
                WHERE symbol = s.symbol AND price <> 0
                  AND created_date BETWEEN s.seed_start AND bk.b
                ORDER BY created_date DESC
                FETCH FIRST 1 ROW WITH TIES
            ) tick ON s.seed_time <= bk.b

            UNION ALL

            SELECT p.hub, 'hub', bk.b, p.ts_start, 1.0
            FROM p CROSS JOIN buckets bk
        ),
        -- Prefer the latest tick through the bucket, or the earliest future tick if no
        -- previous tick exists. Multiple pairs can represent the same asset and source.
        bucket_price AS (
            SELECT DISTINCT ON (asset, source, t) asset, source, t, price
            FROM samples
            ORDER BY asset, source, t,
                     (tick_time > t),
                     CASE WHEN tick_time <= t THEN tick_time END DESC,
                     CASE WHEN tick_time > t THEN tick_time END,
                     price DESC
        ),
        ref_bucket AS MATERIALIZED (
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
