package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.UserCurrencyVolume
import co.nilin.opex.market.core.inout.UserTotalVolumeValue
import co.nilin.opex.market.ports.postgres.model.UserTradeVolumeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface UserTradeVolumeRepository : ReactiveCrudRepository<UserTradeVolumeModel, Long> {

    @Query(
        """
        insert into user_trade_volume (user_id, currency, date, volume, value_usdt, value_irt)
        values (:userId, :currency, :date, :volume, :valueUSDT, :valueIRT)
        on conflict (user_id, currency, date)
        do update 
            set volume = user_trade_volume.volume + EXCLUDED.volume,
                value_usdt = user_trade_volume.value_usdt + EXCLUDED.value_usdt,
                value_irt = user_trade_volume.value_irt + EXCLUDED.value_irt
    """
    )
    fun insertOrUpdate(
        userId: String,
        currency: String,
        date: LocalDate,
        volume: BigDecimal,
        valueUSDT: BigDecimal,
        valueIRT: BigDecimal
    ): Mono<Void>

    @Query(
        """
        select sum(value_usdt) as value_USDT, sum(value_irt) as value_IRT
        from user_trade_volume 
        where user_id = :userId and date >= :startDate
        group by user_id
    """
    )
    fun findTotalValueByUserAndAndDateAfter(userId: String, startDate: LocalDate): Mono<UserTotalVolumeValue>

    @Query(
        """
        select currency, sum(volume) as volume, sum(value_usdt) as value_USDT, sum(value_irt) as value_IRT
        from user_trade_volume 
        where user_id = :userId and date >= :startDate and currency = :currency
        group by currency
    """
    )
    fun findByUserAndCurrencyAndDateAfter(
        userId: String,
        currency: String,
        startDate: LocalDate
    ): Mono<UserCurrencyVolume>
}