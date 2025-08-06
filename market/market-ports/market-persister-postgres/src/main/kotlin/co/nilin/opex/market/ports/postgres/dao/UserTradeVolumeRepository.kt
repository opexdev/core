package co.nilin.opex.market.ports.postgres.dao

import co.nilin.opex.market.core.inout.UserPairVolume
import co.nilin.opex.market.ports.postgres.model.UserTradeVolumeModel
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
        insert into user_trade_volume (user_id, pair, date, value)
        values (:userId, :pair, :date, :value)
        on conflict (user_id, pair, date)
        do update set value = user_trade_volume.value + EXCLUDED.value
    """
    )
    fun insertOrUpdate(userId: String, pair: String, date: LocalDate, value: BigDecimal): Mono<Void>

    @Query(
        """
        select * from user_trade_volume 
        where user_id = :userId
            and pair = :pair
            and date >= :startDate
    """
    )
    fun findByUserAndPairAndDateAfter(userId: String, pair: String, startDate: LocalDate): Mono<UserTradeVolumeModel>

    @Query(
        """
        select pair, sum(value) from user_trade_volume 
        where user_id = :userId and date >= :startDate
        group by pair
    """
    )
    fun findAllByUserAndDateAfter(userId: String, startDate: LocalDate): Flux<UserPairVolume>
}