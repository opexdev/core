package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.model.DailyAmount
import co.nilin.opex.accountant.ports.postgres.model.UserTradeVolumeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface UserTradeVolumeRepository : ReactiveCrudRepository<UserTradeVolumeModel, Long> {

    @Query(
        """
        insert into user_trade_volume (user_id, currency, date, volume, total_amount, quote_currency)
        values (:userId, :currency, :date, :volume, :totalAmount, :quoteCurrency)
        on conflict (user_id, currency, date,quote_currency)
        do update 
            set volume = user_trade_volume.volume + EXCLUDED.volume,
                total_amount = user_trade_volume.total_amount + EXCLUDED.total_amount
    """
    )
    fun insertOrUpdate(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        totalAmount: BigDecimal,
        quoteCurrency: String
    ): Mono<Void>

    @Query(
        """
        select sum(total_amount) as total_amount
        from user_trade_volume 
        where user_id = :userId and date >= :startDate and quote_currency = :quoteCurrency
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
        select sum(total_amount) as total_amount
        from user_trade_volume 
        where user_id = :userId and date >= :startDate and quote_currency = :quoteCurrency and currency = :currency
        group by user_id
    """
    )
    fun findTotalValueByUserAndAndDateAfterAndCurrency(
        userId: String,
        currency: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Mono<BigDecimal>

    @Query(
        """
        select date, sum(total_amount) as total_amount
        from user_trade_volume
        where user_id = :userId
          and date >= :startDate
          and quote_currency = :quoteCurrency
         group by date
        """
    )
    fun findDailyTradeVolume(
        userId: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Flux<DailyAmount>
}