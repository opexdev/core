package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.core.inout.DailyAmount
import co.nilin.opex.wallet.ports.postgres.model.TotalAssetsSnapshotModel
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface TotalAssetsSnapshotRepository : ReactiveCrudRepository<TotalAssetsSnapshotModel, Long> {

    @Modifying
    @Query(
        """
    INSERT INTO total_assets_snapshot(uuid, total_amount, quote_currency, snapshot_date)
    SELECT wo.uuid,
       trunc(SUM(
                     CASE
                         WHEN w.currency = :quoteCurrency THEN w.balance
                         ELSE w.balance * COALESCE(p.price, 0)
                         END
             ), :precision)    AS total_amount,
       :quoteCurrency as quote_currency,
       NOW()          AS snapshot_date
    FROM wallet w
         INNER JOIN public.wallet_owner wo on wo.id = w.owner
         LEFT JOIN price p ON w.currency = p.base_currency and p.quote_currency = :quoteCurrency
    WHERE w.wallet_type != 'CASHOUT'
     AND w.balance > 0
    GROUP BY wo.uuid
    """
    )
    fun createSnapshotsDirectly(quoteCurrency : String ,precision : Int ): Mono<Void>


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

    @Query(
        """
        select snapshot_date as date, total_amount
        from total_assets_snapshot
        where uuid = :userId
          and snapshot_date >= :startDate
          and quote_currency = :quoteCurrency
        order by snapshot_date desc
        """
    )
    fun findDailyBalance(
        userId: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Flux<DailyAmount>

}