package co.nilin.opex.accountant.ports.postgres.dao

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
        insert into user_withdraw_volume (user_id, date, total_amount, quote_currency)
        values (:userId, :date, :totalAmount, :quoteCurrency)
        on conflict (user_id, date,quote_currency)
        do update 
            set total_amount = user_withdraw_volume.total_amount + EXCLUDED.total_amount
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
        from user_withdraw_volume 
        where user_id = :userId and date >= :startDate and quote_currency=:quoteCurrency
        group by user_id
    """
    )
    fun findTotalValueByUserAndAndDateAfter(
        userId: String,
        startDate: LocalDate,
        quoteCurrency: String
    ): Mono<BigDecimal>
}