package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.model.TotalAssetsSnapshot
import co.nilin.opex.wallet.ports.postgres.model.TotalAssetsSnapshotModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface TotalAssetsSnapshotRepository : ReactiveCrudRepository<TotalAssetsSnapshotModel, Long> {


    @Modifying
    @Query(
        """
    INSERT INTO total_assets_snapshot(owner, total_usdt, total_irt, snapshot_date)
    WITH irt_price AS (
        SELECT price AS irt_rate
        FROM price
        WHERE symbol = 'USDT_IRT'
        LIMIT 1
    )
    SELECT
        w.owner,
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
    LEFT JOIN price p ON w.currency || '_USDT' = p.symbol
    CROSS JOIN irt_price irt
    WHERE w.wallet_type != 'CASHOUT'
      AND w.balance > 0
    GROUP BY w.owner
    """
    )
    fun createSnapshotsDirectly(): Mono<Void>


    @Query(
        """
        select * from total_assets_snapshot
         where owner_id = :ownerId 
         and (:startTime is null or snapshot_date > :startTime)
         and (:endTime is null or snapshot_date <= :endTime)
         order by id desc
        """
    )
    fun findByOwnerIdAndSnapshotDate(
        ownerId: Long,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?
    ): Flux<TotalAssetsSnapshotModel>

}