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
    SELECT
        w.owner,
        SUM(w.balance * p.price) AS total_usdt,
        SUM(w.balance * p.price) * (SELECT price FROM price WHERE symbol = 'USDT_IRT') AS total_irt,
        now() AS snapshot_date
    FROM wallet w
    JOIN price p ON w.currency||'_USDT' = p.symbol
    WHERE w.wallet_type != 'CASHOUT' and w.balance > 0
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