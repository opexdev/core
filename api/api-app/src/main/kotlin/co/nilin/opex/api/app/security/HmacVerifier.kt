package co.nilin.opex.api.app.security

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import kotlin.math.abs

@org.springframework.stereotype.Component
class HmacVerifier(
    private val allowedSkew: Duration = Duration.ofMinutes(5)
) {
    data class VerificationInput(
        val method: String,
        val path: String,
        val timestampMillis: Long,
        val bodySha256: String? = null,
        val query: String? = null
    )

    fun verify(secret: String, signatureBase64: String, input: VerificationInput): Boolean {
        // Check timestamp window
        val now = Instant.now().toEpochMilli()
        if (abs(now - input.timestampMillis) > allowedSkew.toMillis()) return false

        val canonical = canonicalString(input)
        val expected = hmacSha256Base64(secret, canonical)
        // Constant-time compare
        return constantTimeEquals(signatureBase64, expected)
    }

    private fun canonicalString(input: VerificationInput): String {
        val sb = StringBuilder()
        sb.append(input.method.uppercase()).append('\n')
            .append(input.path).append('\n')
            .append(input.query ?: "").append('\n')
            .append(input.bodySha256 ?: "").append('\n')
            .append(input.timestampMillis)
        return sb.toString()
    }

    private fun hmacSha256Base64(secret: String, data: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val raw = mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(raw)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(StandardCharsets.UTF_8)
        val bBytes = b.toByteArray(StandardCharsets.UTF_8)
        var result = aBytes.size xor bBytes.size
        val len = minOf(aBytes.size, bBytes.size)
        for (i in 0 until len) {
            result = result or (aBytes[i].toInt() xor bBytes[i].toInt())
        }
        return result == 0
    }
}