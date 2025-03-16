package co.nilin.opex.otp.app.service

import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.repository.OTPConfigRepository
import co.nilin.opex.otp.app.repository.OTPRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class OTPService(
    private val repository: OTPRepository,
    private val configRepository: OTPConfigRepository
) {

    suspend fun requestOTP(subject: String, type: OTPType) {
        val config = configRepository.findById(type).awaitSingleOrNull()
            ?: throw IllegalStateException("Config for type $type not found")

    }

    suspend fun verifyOTP(code: String, tracingCode: String): Boolean {
        return true
    }
}