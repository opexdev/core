package co.nilin.opex.api.app.service

import co.nilin.opex.api.app.proxy.AuthProxy
import co.nilin.opex.api.ports.postgres.dao.APIKeyRepository
import co.nilin.opex.api.ports.postgres.model.APIKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.util.function.Tuple2
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class APIKeyService(
    private val apiKeyRepository: APIKeyRepository,
    private val authProxy: AuthProxy,
    @Value
) {

    fun createAPIKey(
        userId: String,
        label: String,
        expirationTime: LocalDateTime,
        allowedIPs: String,
        currentToken: String
    ): Pair<String, APIKey> {
        val secret = generateSecret()
        val accessToken = authProxy.exchangeToken(clien)
    }

    private fun encryptAES(input: String, key: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(ByteArray(16)))
        }
        val cipherText = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(cipherText)
    }

    private fun decryptAES(cipherText: String, key: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(ByteArray(16)))
        }
        val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(plainText)
    }

    private fun generateSecret(length: Int = 32): String {
        val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { chars.random() }.joinToString("")
    }

}