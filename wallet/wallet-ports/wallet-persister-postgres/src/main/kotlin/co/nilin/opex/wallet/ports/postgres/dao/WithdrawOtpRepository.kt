package co.nilin.opex.wallet.ports.postgres.dao

import co.nilin.opex.wallet.ports.postgres.model.WithdrawOtpModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface WithdrawOtpRepository : ReactiveCrudRepository<WithdrawOtpModel, Long> {

    @Query("select * from withdraws_otp where withdraw=:withdrawId order by id desc")
    suspend fun findByWithdrawId(withdrawId: Long): Flux<WithdrawOtpModel>
}