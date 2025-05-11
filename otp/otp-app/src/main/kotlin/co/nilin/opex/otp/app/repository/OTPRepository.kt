package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPType
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OTPRepository : CoroutineCrudRepository<OTP, Long> {

    suspend fun findByTracingCode(traceCode: String): OTP?

    @Query("select * from otp where user_id = :userId and is_active is true")
    suspend fun findActiveByUserId(userId: String): OTP?

    @Query("select * from otp where is_active is true and ((receiver = :receiver and type = :type) or user_id = :userId)")
    suspend fun findActiveByReceiverAndTypeOrUserId(receiver: String, type: OTPType, userId: String): OTP?

    @Query("update otp set is_active = false where id = :id")
    suspend fun markInactive(id: Long)

    @Query("update otp set is_verified = true, is_active = false, verify_time = current_timestamp where id = :id")
    suspend fun markVerified(id: Long)

}