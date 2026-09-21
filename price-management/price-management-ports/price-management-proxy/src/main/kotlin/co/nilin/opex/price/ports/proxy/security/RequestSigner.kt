package co.nilin.opex.price.ports.proxy.security

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Signs outgoing requests the way rate-scanner expects: HMAC-SHA256 over
 * "timestamp\nMETHOD\npath\nrawQuery\nsha256hex(body)" (hex-encoded), on top of the X-API-Key
 * header. rawQuery is "" when there's no query string, and sha256hex(body) is the hash of an
 * empty byte array when there's no body (e.g. GET requests).
 */
object RequestSigner {

    private const val HMAC_ALGORITHM = "HmacSHA256"

    fun sign(
        secretKey: String,
        timestamp: Long,
        method: String,
        path: String,
        rawQuery: String,
        body: ByteArray
    ): String {
        val payload = listOf(
            timestamp.toString(),
            method.uppercase(),
            path,
            rawQuery,
            sha256Hex(body)
        ).joinToString("\n")
        return hmacSha256Hex(secretKey, payload)
    }

    private fun sha256Hex(body: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(body).toHex()

    private fun hmacSha256Hex(secretKey: String, payload: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM))
        return mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8)).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
