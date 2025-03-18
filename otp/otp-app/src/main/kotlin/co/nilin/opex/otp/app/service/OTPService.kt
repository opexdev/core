package co.nilin.opex.otp.app.service

import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.repository.OTPConfigRepository
import co.nilin.opex.otp.app.repository.OTPRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

@Component
class OTPService(
    private val repository: OTPRepository,
    private val configRepository: OTPConfigRepository
) {

    private val logger by LoggerDelegate()
    private val encoder = BCryptPasswordEncoder()

    suspend fun requestOTP(receiver: String, type: OTPType): String {
        val config = configRepository.findById(type).awaitSingleOrNull()
            ?: throw IllegalStateException("Config for type $type not found")

        val currentOtp = repository.findByReceiverAndType(receiver, type)
        if (currentOtp != null && !currentOtp.isExpired()) {
            throw IllegalStateException("Otp already requested for receiver: $receiver and type: $type")
        }

        val expireTime = LocalDateTime.now().plusSeconds(config.expireTimeSeconds.toLong())
        val newOtp = OTP(
            generateCode(config.charCount, config.includeAlphabetChars).encode(),
            receiver,
            UUID.randomUUID().toString(),
            type,
            expireTime
        )

        val otp = repository.save(newOtp)
        //todo send code to receiver
        return otp.tracingCode
    }

    suspend fun verifyOTP(code: String, tracingCode: String): Boolean {
        val otp = repository.findByTracingCode(tracingCode)
        return verifyOtp(code, otp)
    }

    suspend fun verifyOtp(code: String, receiver: String, type: OTPType): Boolean {
        val otp = repository.findByReceiverAndType(receiver, type)
        return verifyOtp(code, otp)
    }

    private suspend fun verifyOtp(code: String, otp: OTP?): Boolean {
        if (otp == null) {
            logger.warn("Otp request not found")
            return false
        }

        if (otp.isExpired()) {
            logger.warn("Otp request expired. tracingCode: ${otp.tracingCode}")
            return false
        }

        if (code.encode() == otp.code) {
            repository.deleteById(otp.id!!)
            return true
        }
        return false
    }

    private suspend fun generateCode(length: Int): String {
        val min = 10.0.pow(length - 1).toInt()
        val max = 10.0.pow(length).toInt() - 1
        return Random.nextInt(min, max + 1).toString()
    }

    private fun generateCode(length: Int, includeAlpha: Boolean): String {
        val chars = if (includeAlpha) ('A'..'Z') + ('a'..'z') + ('0'..'9') else ('0'..'9').toList()
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun String.encode(): String {
        return encoder.encode(this)
    }
}