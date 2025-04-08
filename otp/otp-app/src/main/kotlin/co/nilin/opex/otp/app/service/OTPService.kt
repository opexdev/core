package co.nilin.opex.otp.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.repository.OTPConfigRepository
import co.nilin.opex.otp.app.repository.OTPRepository
import co.nilin.opex.otp.app.service.message.MessageManager
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
    private val configRepository: OTPConfigRepository,
    private val messageManager: MessageManager,
    private val encoder: BCryptPasswordEncoder
) {

    private val logger by LoggerDelegate()

    suspend fun requestOTP(receiver: String, type: OTPType): String {
        val config = configRepository.findById(type).awaitSingleOrNull()
            ?: throw OpexError.OTPConfigNotFound.exception()

        // Check whether receiver has an active otp of specified type
        repository.findActiveByReceiverAndType(receiver, type)?.let {
            if (it.isExpired())
                repository.markInactive(it.id!!)
            else
                throw OpexError.OTPAlreadyRequested.exception()
        }

        val expireTime = LocalDateTime.now().plusSeconds(config.expireTimeSeconds.toLong())
        val code = generateCode(config.charCount, config.includeAlphabetChars)
        val newOtp = OTP(
            code.encode(),
            receiver,
            UUID.randomUUID().toString(),
            type,
            expireTime
        )

        logger.debug("${newOtp.tracingCode} -> $code")
        messageManager.sendMessage(config, type, code, receiver)
        val otp = repository.save(newOtp)
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

        if (encoder.matches(code, otp.code)) {
            repository.markVerified(otp.id!!)
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