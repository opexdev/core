package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.model.DailyAmount
import co.nilin.opex.accountant.ports.postgres.model.UserDepositVolumeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface UserDepositVolumeRepository : ReactiveCrudRepository<UserDepositVolumeModel, Long> {

    @Query(
        """
        insert into user_deposit_volume (user_id, date, total_amount, quote_currency)
        values (:userId, :date, :totalAmount, :quoteCurrency)
        on conflict (user_id, date,quote_currency)
        do update 
            set total_amount = user_deposit_volume.total_amount + EXCLUDED.total_amount
    """
    )
    fun insertOrUpdate(
        userId: String,
        date: LocalDate,
        totalAmount: BigDecimal,
        quoteCurrency: String
    ): Mono<Void>


    @Query(
        """
        select sum(total_amount) as total_Amount
        from user_deposit_volume 
        where user_id = :userId and date >= :startDate and quote_currency=:quoteCurrency
        group by user_id
    """
    )
    fun findTotalValueByUserAndAndDateAfter(
        userId: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Mono<BigDecimal>


    @Query(
        """
        select date, total_amount
        from user_deposit_volume
        where user_id = :userId
          and date >= :startDate
          and quote_currency = :quoteCurrency
        order by date desc
        """
    )
    fun findDailyDepositVolume(
        userId: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Flux<DailyAmount>
}