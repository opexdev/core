package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.UserDetailAssetsSnapshotRaw
import co.nilin.opex.wallet.ports.postgres.model.DetailAssetsSnapshotModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface DetailAssetsSnapshotRepository : ReactiveCrudRepository<DetailAssetsSnapshotModel, Long> {

    @Modifying
    @Query(
        """
    INSERT INTO detail_assets_snapshot(uuid, currency, volume, total_amount, quote_currency, snapshot_date,batch_number)
    SELECT wo.uuid,
           w.currency,
           SUM(w.balance) AS volume,
           trunc(SUM(
                CASE
                    WHEN w.currency = :quoteCurrency THEN w.balance
                    ELSE w.balance * COALESCE(p.price, 0)
                END
           ), :precision) AS total_amount,
           :quoteCurrency AS quote_currency,
           NOW() AS snapshot_date,
           COALESCE((SELECT MAX(batch_number) FROM detail_assets_snapshot), 0) + 1 AS batch_number
    FROM wallet w
         INNER JOIN public.wallet_owner wo ON wo.id = w.owner
         LEFT JOIN price p 
            ON w.currency = p.base_currency 
           AND p.quote_currency = :quoteCurrency
    WHERE w.balance > 0
    GROUP BY wo.uuid, w.currency
    """
    )
    fun createDetailSnapshotsDirectly(
        quoteCurrency: String,
        precision: Int
    ): Mono<Void>


    @Query(
        """
    SELECT t.uuid AS uuid,
           JSON_AGG(
                   JSON_BUILD_OBJECT(
                           'currency', t.currency,
                           'volume', trim_scale(trunc(t.volume, c.precision::int))
                   )
           )                                AS currency_snapshots,
           SUM(t.total_amount)              AS total_amount,
           t.quote_currency AS quote_currency,
           t.snapshot_date AS snapshot_date
    FROM detail_assets_snapshot t
        INNER JOIN public.wallet_owner wo ON wo.uuid = t.uuid
        INNER JOIN public.currency c ON c.symbol = t.currency
    WHERE t.batch_number = (SELECT MAX(batch_number)FROM detail_assets_snapshot) AND wo.level = '1'
    GROUP BY t.uuid, t.quote_currency, t.snapshot_date
    ORDER BY total_amount DESC
    limit :limit
    offset :offset
        """
    )
    fun findAllLatestSnapshots(
        limit: Int,
        offset: Int,
    ): Flux<UserDetailAssetsSnapshotRaw>
}