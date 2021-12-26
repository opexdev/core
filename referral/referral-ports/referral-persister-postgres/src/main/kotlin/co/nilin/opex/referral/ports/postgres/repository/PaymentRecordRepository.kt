package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.core.model.PaymentStatuses
import co.nilin.opex.referral.ports.postgres.dao.PaymentRecord
import co.nilin.opex.referral.ports.postgres.dao.PaymentRecordProjected
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PaymentRecordRepository : ReactiveCrudRepository<PaymentRecord, Long> {
    suspend fun findByPaymentStatus(paymentStatus: PaymentStatuses): Flux<PaymentRecord>

    @Query("SELECT * FROM payment_records LEFT JOIN commission_rewards ON commission_rewards_id = commission_rewards.id WHERE payment_status = :paymentStatus")
    suspend fun findByPaymentStatusProjected(paymentStatus: PaymentStatuses): Flux<PaymentRecordProjected>

    @Modifying
    @Query("UPDATE payment_records SET payment_status = :paymentStatus WHERE id = :id")
    suspend fun updatePaymentStatusById(id: Long, paymentStatus: PaymentStatuses)
}