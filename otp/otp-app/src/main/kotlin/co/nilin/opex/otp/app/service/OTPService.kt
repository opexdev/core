package co.nilin.opex.otp.app.service

import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import co.nilin.opex.otp.app.data.OTPCode
import co.nilin.opex.otp.app.data.OTPReceiver
import co.nilin.opex.otp.app.model.OTP
import co.nilin.opex.otp.app.model.OTPConfig
import co.nilin.opex.otp.app.model.OTPType
import co.nilin.opex.otp.app.repository.OTPConfigRepository
import co.nilin.opex.otp.app.repository.OTPRepository
import co.nilin.opex.otp.app.service.message.MessageManager
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.pow
import kotlin.random.Random

@Service
class OTPService(
    private val repository: OTPRepository,
    private val configRepository: OTPConfigRepository,
    private val messageManager: MessageManager,
    private val encoder: BCryptPasswordEncoder
) {

    private val logger by LoggerDelegate()

    suspend fun requestOTP(receiver: String, type: OTPType, userId: String, action: String?): String {
        checkActiveOTP(receiver, type, userId)
        val config = getConfig(type)
        val code = generateCode(config.charCount, config.includeAlphabetChars)
        messageManager.sendMessage(config, type, code, receiver)
        return storeOTP(receiver, type, code, config, userId, action)
    }

    suspend fun requestCompositeOTP(receivers: Set<OTPReceiver>, userId: String, action: String?): String {
        val type = OTPType.COMPOSITE
        val mainConfig = getConfig(type)
        val receiver = receivers.joinToString(",") { it.receiver }
        checkActiveOTP(receiver, type, userId)

        val compositeCode = StringBuilder()
        receivers.forEach {
            val config = getConfig(it.type)
            val code = generateCode(config.charCount, config.includeAlphabetChars)
            messageManager.sendMessage(config, type, code, receiver)
            compositeCode.append(code)
        }

        return storeOTP(receiver, type, compositeCode.toString(), mainConfig, userId, action)
    }

    private suspend fun storeOTP(
        receiver: String,
        type: OTPType,
        code: String,
        config: OTPConfig,
        userId: String,
        action: String?
    ): String {
        val expireTime = LocalDateTime.now().plusSeconds(config.expireTimeSeconds.toLong())
        val newOtp = OTP(
            code.encode(),
            receiver,
            userId,
            action ?: "UNSPECIFIED",
            UUID.randomUUID().toString(),
            type,
            expireTime,
        )

        logger.debug("${newOtp.tracingCode} -> $code")
        val otp = repository.save(newOtp)
        return otp.tracingCode
    }

    private suspend fun checkActiveOTP(receiver: String, type: OTPType, userId: String) {
        // Check whether receiver has an active otp of specified type
        repository.findActiveByReceiverAndTypeOrUserId(receiver, type, userId)?.let {
            if (it.isExpired())
                repository.markInactive(it.id!!)
            else
                throw OpexError.OTPAlreadyRequested.exception()
        }
    }

    private suspend fun getConfig(type: OTPType): OTPConfig {
        return configRepository.findById(type).awaitSingleOrNull()
            ?: throw OpexError.OTPConfigNotFound.exception()
    }

    suspend fun verifyOTP(code: String, userId: String): Boolean {
        val otp = repository.findActiveByUserId(userId)
        return verifyOtp(code, otp, userId)
    }

    suspend fun verifyCompositeOTP(codes: Set<OTPCode>, userId: String): Boolean {
        repository.findActiveByUserId(userId)?.let {
            if (it.type != OTPType.COMPOSITE)
                throw OpexError.BadRequest.exception()

            val code = reconstructCode(codes)
            return verifyOtp(code, it, userId)
        }
        return false
    }

    @Deprecated("Use userId instead")
    suspend fun verifyOTP(code: String, userId: String, tracingCode: String?): Boolean {
        val otp = repository.findByTracingCode(tracingCode!!)
        return verifyOtp(code, otp, userId)
    }

    @Deprecated("Use userId instead")
    suspend fun verifyCompositeOTP(codes: Set<OTPCode>, userId: String, tracingCode: String?): Boolean {
        repository.findByTracingCode(tracingCode!!)?.let {
            if (it.type != OTPType.COMPOSITE)
                throw OpexError.BadRequest.exception()

            val code = reconstructCode(codes)
            return verifyOtp(code, it, userId)
        }
        return false
    }

    private suspend fun reconstructCode(codes: Set<OTPCode>): String {
        return codes.sortedBy { it.type.compositeOrder }.joinToString("") { it.code }
    }

    private suspend fun verifyOtp(code: String, otp: OTP?, userId: String): Boolean {
        if (otp == null) {
            logger.warn("Otp request not found")
            return false
        }

        if (otp.userId != userId) {
            logger.warn("Otp userId mismatch")
            return false
        }

        if (otp.isExpired()) {
            logger.warn("Otp request expired. tracingCode: ${otp.tracingCode}")
            return false
        }

        if (!encoder.matches(code, otp.code)) {
            logger.warn("Otp request invalid")
            return false
        }

        repository.markVerified(otp.id!!)
        return true
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