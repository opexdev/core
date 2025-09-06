package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.TotalAssetsSnapshotModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TotalAssetsSnapshotRepository : ReactiveCrudRepository<TotalAssetsSnapshotModel, Long> {


    @Modifying
    @Query(
        """
    INSERT INTO total_assets_snapshot(uuid, total_usdt, total_irt, snapshot_date)
    WITH irt_price AS (
        SELECT price AS irt_rate
        FROM price
        WHERE symbol = 'USDT_IRT'
        LIMIT 1
    )
    SELECT
        wo.uuid,
        trunc(SUM(
            CASE
                WHEN w.currency = 'USDT' THEN w.balance
                WHEN w.currency = 'IRT' THEN w.balance / irt.irt_rate
                ELSE w.balance * COALESCE(p.price, 0)
            END
        ), 2) AS total_usdt,
        trunc(SUM(
            CASE
                WHEN w.currency = 'USDT' THEN w.balance * irt.irt_rate
                WHEN w.currency = 'IRT' THEN w.balance
                ELSE w.balance * COALESCE(p.price, 0) * irt.irt_rate
            END
        ), 0) AS total_irt,
        NOW() AS snapshot_date
    FROM wallet w
    INNER JOIN public.wallet_owner wo on wo.id = w.owner
    LEFT JOIN price p ON w.currency || '_USDT' = p.symbol
    CROSS JOIN irt_price irt
    WHERE w.wallet_type != 'CASHOUT'
      AND w.balance > 0
    GROUP BY wo.uuid
    """
    )
    fun createSnapshotsDirectly(): Mono<Void>


    @Query(
        """
        select * from total_assets_snapshot
         where uuid = :uuid 
         order by id desc
         limit 1
        """
    )
    fun findLastSnapshotByUuid(
        uuid: String,
    ): Mono<TotalAssetsSnapshotModel>
}