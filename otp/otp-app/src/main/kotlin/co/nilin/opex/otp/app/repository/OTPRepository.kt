package co.nilin.opex.otp.app.repository

import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPType
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface OTPRepository : CoroutineCrudRepository<OTP, Long> {

    suspend fun findByTracingCode(traceCode: String): OTP?

    suspend fun findBySubjectAndType(subject: String, type: OTPType): OTP?

}