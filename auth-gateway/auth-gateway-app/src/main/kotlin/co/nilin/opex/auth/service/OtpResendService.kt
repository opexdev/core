package co.nilin.opex.auth.service

import co.nilin.opex.auth.model.OTPAction
import co.nilin.opex.auth.model.OTPReceiver
import co.nilin.opex.auth.utils.RedisCacheHelper
import co.nilin.opex.common.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

@Service
class OtpResendService(
    private val redisCacheHelper: RedisCacheHelper,
    @Value("\${app.otp.resend.limit}") private val limit: Int,
    @Value("\${app.otp.resend.window-seconds}") private val windowSeconds: Long,
) {
    private val secureRandom = SecureRandom()
    private val tokenEncoder = Base64.getUrlEncoder().withoutPadding()

    suspend fun issue(
        action: OTPAction,
        receiver: OTPReceiver,
        captchaAcceptedAt: Instant,
        accountId: String? = null,
    ): String {
        val expiresAt = captchaAcceptedAt.plusSeconds(windowSeconds)
        val ttl = Duration.between(Instant.now(), expiresAt)
        if (ttl.toMillis() <= 0) throw OpexError.CaptchaRequired.exception()

        val tokenBytes = ByteArray(48)
        secureRandom.nextBytes(tokenBytes)
        val token = tokenEncoder.encodeToString(tokenBytes)
        val grant = "$limit:${expiresAt.toEpochMilli()}"
        redisCacheHelper.putIfAbsent(key(action, receiver, token, accountId), grant, ttl)
        return token
    }

    suspend fun <T> resend(
        action: OTPAction,
        receiver: OTPReceiver,
        token: String,
        accountId: String? = null,
        send: suspend () -> T,
    ): T {
        if (token.isBlank()) throw OpexError.CaptchaRequired.exception()
        val slotKey = reserveSlot(key(action, receiver, token, accountId))

        try {
            return send()
        } catch (ex: OpexException) {
            if (ex.error != OpexError.OTPAlreadyRequested) throw ex

            try {
                withContext(NonCancellable) { redisCacheHelper.evict(slotKey) }
            } catch (refundFailure: Exception) {
                ex.addSuppressed(refundFailure)
            }
            throw ex
        }
    }

    private suspend fun reserveSlot(grantKey: String): String {
        val grant = redisCacheHelper.get(grantKey) ?: throw OpexError.CaptchaRequired.exception()
        val ttl = redisCacheHelper.getTtl(grantKey) ?: throw OpexError.CaptchaRequired.exception()
        val maxResends = grant.substringBefore(':').toInt()

        for (slot in 1..maxResends) {
            val slotKey = "$grantKey:$grant:$slot"
            if (redisCacheHelper.putIfAbsent(slotKey, "reserved", ttl)) {
                if (redisCacheHelper.get(grantKey) == grant) return slotKey
                break
            }
        }
        throw OpexError.CaptchaRequired.exception()
    }

    private fun key(action: OTPAction, receiver: OTPReceiver, token: String, accountId: String?): String {
        val scope = if (accountId == null) action.name else "${action.name}\u0000$accountId"
        val binding = "$scope\u0000${receiver.type.name}\u0000${receiver.receiver}\u0000$token"
        val digest = MessageDigest.getInstance("SHA-256").digest(binding.toByteArray())
        return "auth:otp-resend:grant:${HexFormat.of().formatHex(digest)}"
    }
}
