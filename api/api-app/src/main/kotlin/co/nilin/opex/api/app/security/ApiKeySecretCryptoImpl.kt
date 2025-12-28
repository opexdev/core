package co.nilin.opex.api.app.security

import co.nilin.opex.api.core.spi.ApiKeySecretCrypto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class ApiKeySecretCryptoImpl(
    @Value("\${app.api.crypto.key}") private val base64Key: String
) : ApiKeySecretCrypto {
    private val key: SecretKey
    private val rng = SecureRandom()

    init {
        val decoded = Base64.getDecoder().decode(base64Key)
        require(decoded.size == 16 || decoded.size == 24 || decoded.size == 32) {
            "app.api.crypto.key must be 128/192/256-bit Base64 key"
        }
        key = SecretKeySpec(decoded, "AES")
    }

    override fun encrypt(plaintext: String): String {
        val iv = ByteArray(12)
        rng.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val ivB64 = Base64.getEncoder().encodeToString(iv)
        val ctB64 = Base64.getEncoder().encodeToString(ct)
        return "$ivB64:$ctB64"
    }

    override fun decrypt(ciphertext: String): String {
        val parts = ciphertext.split(":")
        require(parts.size == 2) { "Invalid encrypted secret format" }
        val iv = Base64.getDecoder().decode(parts[0])
        val ct = Base64.getDecoder().decode(parts[1])
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        val pt = cipher.doFinal(ct)
        return String(pt, Charsets.UTF_8)
    }
}
