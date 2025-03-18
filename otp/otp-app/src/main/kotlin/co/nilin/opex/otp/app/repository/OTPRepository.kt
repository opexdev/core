package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OTPRepository : CoroutineCrudRepository<OTP, Long> {

    suspend fun findByTracingCode(traceCode: String): OTP?

    suspend fun findByReceiverAndType(receiver: String, type: OTPType): OTP?

}