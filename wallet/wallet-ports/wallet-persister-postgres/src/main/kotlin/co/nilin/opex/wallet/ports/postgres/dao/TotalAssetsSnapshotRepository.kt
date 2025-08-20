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
    SELECT w.owner,
       trunc(SUM(
           COALESCE(p.price * w.balance, 0)
           + CASE WHEN w.currency = 'USDT' THEN w.balance ELSE 0 END
           + CASE WHEN w.currency = 'IRT' THEN w.balance / irt.price ELSE 0 END
       ),2) AS total_usdt,
       trunc(SUM(
           COALESCE(p.price * w.balance, 0) * irt.price
           + CASE WHEN w.currency = 'IRT' THEN w.balance ELSE 0 END
           + CASE WHEN w.currency = 'USDT' THEN w.balance * irt.price ELSE 0 END
       )) AS total_irt,
       now() AS snapshot_date
    FROM wallet w
    LEFT JOIN price p ON concat(w.currency,'_USDT') = p.symbol
    CROSS JOIN (SELECT price FROM price WHERE symbol = 'USDT_IRT') irt
    WHERE w.wallet_type != 'CASHOUT' AND w.balance > 0
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