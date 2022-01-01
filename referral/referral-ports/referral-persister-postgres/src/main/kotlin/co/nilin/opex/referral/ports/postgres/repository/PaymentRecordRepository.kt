package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.core.model.PaymentStatuses
import co.nilin.opex.referral.ports.postgres.dao.PaymentRecord
import co.nilin.opex.referral.ports.postgres.dao.PaymentRecordProjected
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.math.BigDecimal

@Repository
interface PaymentRecordRepository : PaymentRecordProjectedRepository, ReactiveCrudRepository<PaymentRecord, Long> {
    suspend fun findByPaymentStatus(paymentStatus: PaymentStatuses): Flux<PaymentRecord>
    suspend fun findByPaymentStatusWhereCreateDateLessThan(
        paymentStatus: PaymentStatuses,
        createData: Long
    ): Flux<PaymentRecord>

    @Modifying
    @Query("INSERT INTO payment_records(commission_reward_id, payment_status) VALUES (:id, :paymentStatus)")
    suspend fun updatePaymentStatusById(id: Long, paymentStatus: PaymentStatuses)

    @Modifying
    @Query("INSERT INTO payment_records(commission_reward_id, transfer_ref) VALUES (:id, :transferRef)")
    suspend fun checkout(id: Long, transferRef: String)
}

interface PaymentRecordProjectedRepository {
    @Query("SELECT * FROM payment_records_projected WHERE payment_status = :paymentStatus")
    suspend fun findByPaymentStatusProjected(paymentStatus: PaymentStatuses): Flux<PaymentRecordProjected>

    @Query("SELECT * FROM payment_records_projected WHERE payment_status = :paymentStatus AND create_date < :createDate")
    suspend fun findByPaymentStatusWhereCreateDateLessThanProjected(
        paymentStatus: PaymentStatuses,
        createData: Long
    ): Flux<PaymentRecordProjected>

    @Query("SELECT *, SUM(share) AS acc_share OVER (PARTITION BY uuid) FROM payment_records_projected WHERE payment_status = 'pending' AND acc_share >= :value")
    suspend fun findAllWhereTotalShareMoreThanProjected(value: BigDecimal): Flux<PaymentRecordProjected>

    @Query("SELECT *, SUM(share) AS acc_share OVER (PARTITION BY uuid) FROM payment_records_projected WHERE payment_status = 'pending' AND acc_share >= :value AND uuid = :uuid")
    suspend fun findByUuidWhereTotalShareMoreThanProjected(
        uuid: String,
        value: BigDecimal
    ): Flux<PaymentRecordProjected>
}
