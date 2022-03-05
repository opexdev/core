package co.nilin.opex.referral.ports.postgres.repository

import co.nilin.opex.referral.core.model.CheckoutState
import co.nilin.opex.referral.ports.postgres.dao.CheckoutRecord
import co.nilin.opex.referral.ports.postgres.dao.CheckoutRecordProjected
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.util.*

@Repository
interface CheckoutRecordRepository : CheckoutRecordProjectedRepository, ReactiveCrudRepository<CheckoutRecord, Long> {
    @Modifying
    @Query("INSERT INTO checkout_records(commission_rewards_id, checkout_state) VALUES (:id, :checkoutState)")
    suspend fun updateCheckoutStateById(id: Long, checkoutState: CheckoutState)

    @Modifying
    @Query("INSERT INTO checkout_records(commission_rewards_id, transfer_ref, checkout_state) VALUES (:id, :transferRef, 'CHECKED_OUT')")
    suspend fun checkout(id: Long, transferRef: String)
}

interface CheckoutRecordProjectedRepository {
    @Query("SELECT * FROM checkout_records_projected WHERE checkout_state = :checkoutState")
    suspend fun findByCheckoutStateProjected(checkoutState: CheckoutState): Flux<CheckoutRecordProjected>

    @Query("SELECT * FROM checkout_records_projected WHERE checkout_state = :checkoutState AND create_date < :createDate")
    suspend fun findByCheckoutStateWhereCreateDateLessThanProjected(
        checkoutState: CheckoutState,
        createData: Date
    ): Flux<CheckoutRecordProjected>

    @Query(
        """
            WITH s AS (
                SELECT *, SUM(share) OVER (PARTITION BY rewarded_uuid) AS acc_share 
                FROM checkout_records_projected 
                WHERE checkout_state = 'PENDING'
            ) 
            SELECT * FROM s WHERE acc_share >= :value
        """
    )
    suspend fun findAllWhereTotalShareMoreThanProjected(value: BigDecimal): Flux<CheckoutRecordProjected>

    @Query(
        """
            WITH s AS (
                SELECT *, SUM(share) OVER (PARTITION BY rewarded_uuid) AS acc_share 
                FROM checkout_records_projected 
                WHERE checkout_state = 'PENDING' AND rewarded_uuid = :uuid
            ) 
            SELECT * FROM s WHERE acc_share >= :value
        """
    )
    suspend fun findByUuidWhereTotalShareMoreThanProjected(
        uuid: String,
        value: BigDecimal
    ): Flux<CheckoutRecordProjected>
}
