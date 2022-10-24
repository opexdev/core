package co.nilin.opex.api.app.service

import co.nilin.opex.api.app.proxy.AuthProxy
import co.nilin.opex.api.ports.postgres.dao.APIKeyRepository
import co.nilin.opex.api.ports.postgres.model.APIKey
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
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
    @Value("\${app.auth.api-key-client.secret}")
    private val clientSecret: String
) {

    suspend fun createAPIKey(
        userId: String,
        label: String,
        expirationTime: LocalDateTime,
        allowedIPs: String,
        currentToken: String
    ): Pair<String, APIKey> {
        if (apiKeyRepository.countByUserId(userId).awaitFirstOrElse { 0 } >= 10)
            throw OpexException(OpexError.APIKeyLimitReached)

        val secret = generateSecret()
        val tokenResponse = authProxy.exchangeToken(clientSecret, currentToken)
        val apiKey = apiKeyRepository.save(
            APIKey(
                null,
                userId,
                label,
                encryptAES(tokenResponse.access_token, secret),
                encryptAES(tokenResponse.refresh_token, secret),
                expirationTime,
                allowedIPs
            )
        ).awaitSingle()
        return Pair(secret, apiKey)
    }

    suspend fun getAccessToken(key: String, secret: String): String? {
        val apiKey = apiKeyRepository.findByKey(key).awaitSingleOrNull()
        return if (apiKey == null)
            null
        else
            decryptAES(apiKey.accessToken, secret)
    }

    suspend fun getKeysByUserId(userId: String): List<APIKey> {
        return apiKeyRepository.findAllByUserId(userId).collectList().awaitFirstOrElse { emptyList() }
    }

    suspend fun changeKeyState(userId: String, key: String, isEnabled: Boolean) {
        val apiKey = apiKeyRepository.findByKey(key).awaitSingleOrNull() ?: throw OpexException(OpexError.NotFound)
        if (apiKey.userId != userId)
            throw OpexException(OpexError.Forbidden)
        apiKey.isEnabled = isEnabled
        apiKeyRepository.save(apiKey).awaitSingle()
    }

    suspend fun deleteKey(userId: String, key: String) {
        val apiKey = apiKeyRepository.findByKey(key).awaitSingleOrNull() ?: throw OpexException(OpexError.NotFound)
        if (apiKey.userId != userId)
            throw OpexException(OpexError.Forbidden)
        apiKeyRepository.delete(apiKey).awaitSingle()
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