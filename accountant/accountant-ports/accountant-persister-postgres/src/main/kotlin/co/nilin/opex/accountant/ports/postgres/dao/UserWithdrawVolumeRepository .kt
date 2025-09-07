package co.nilin.opex.accountant.ports.postgres.dao

import co.nilin.opex.accountant.core.inout.UserTotalVolumeValue
import co.nilin.opex.accountant.ports.postgres.model.UserWithdrawVolumeModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

@Repository
interface UserWithdrawVolumeRepository : ReactiveCrudRepository<UserWithdrawVolumeModel, Long> {

    @Query(
        """
        insert into user_withdraw_volume (user_id, date, value_usdt, value_irt)
        values (:userId, :date, :valueUSDT, :valueIRT)
        on conflict (user_id, date)
        do update 
            set value_usdt = user_withdraw_volume.value_usdt + EXCLUDED.value_usdt,
                value_irt = user_withdraw_volume.value_irt + EXCLUDED.value_irt
    """
    )
    fun insertOrUpdate(
        userId: String,
        date: LocalDate,
        valueUSDT: BigDecimal,
        valueIRT: BigDecimal
    ): Mono<Void>


    @Query(
        """
        select sum(value_usdt) as value_USDT, sum(value_irt) as value_IRT
        from user_withdraw_volume 
        where user_id = :userId and date >= :startDate
        group by user_id
    """
    )
    fun findTotalValueByUserAndAndDateAfter(userId: String, startDate: LocalDate): Mono<UserTotalVolumeValue>

}