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
import java.util.*

@Repository
interface PaymentRecordRepository : PaymentRecordProjectedRepository, ReactiveCrudRepository<PaymentRecord, Long> {
    @Modifying
    @Query("INSERT INTO payment_records(commission_rewards_id, payment_status) VALUES (:id, :paymentStatus)")
    suspend fun updatePaymentStatusById(id: Long, paymentStatus: PaymentStatuses)

    @Modifying
    @Query("INSERT INTO payment_records(commission_rewards_id, transfer_ref, payment_status) VALUES (:id, :transferRef, 'CHECKED_OUT')")
    suspend fun checkout(id: Long, transferRef: String)
}

interface PaymentRecordProjectedRepository {
    @Query("SELECT * FROM payment_records_projected WHERE payment_status = :paymentStatus")
    suspend fun findByPaymentStatusProjected(paymentStatus: PaymentStatuses): Flux<PaymentRecordProjected>

    @Query("SELECT * FROM payment_records_projected WHERE payment_status = :paymentStatus AND create_date < :createDate")
    suspend fun findByPaymentStatusWhereCreateDateLessThanProjected(
        paymentStatus: PaymentStatuses,
        createData: Date
    ): Flux<PaymentRecordProjected>

    @Query(
        """
            WITH s AS (
                SELECT *, SUM(share) OVER (PARTITION BY rewarded_uuid) AS acc_share 
                FROM payment_records_projected 
                WHERE payment_status = 'PENDING'
            ) 
            SELECT * FROM s WHERE acc_share >= :value
        """
    )
    suspend fun findAllWhereTotalShareMoreThanProjected(value: BigDecimal): Flux<PaymentRecordProjected>

    @Query(
        """
            WITH s AS (
                SELECT *, SUM(share) OVER (PARTITION BY rewarded_uuid) AS acc_share 
                FROM payment_records_projected 
                WHERE payment_status = 'PENDING' AND rewarded_uuid = :uuid
            ) 
            SELECT * FROM s WHERE acc_share >= :value
        """
    )
    suspend fun findByUuidWhereTotalShareMoreThanProjected(
        uuid: String,
        value: BigDecimal
    ): Flux<PaymentRecordProjected>
}
