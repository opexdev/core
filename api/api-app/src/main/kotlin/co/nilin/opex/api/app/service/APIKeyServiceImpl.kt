package co.nilin.opex.api.app.service

import co.nilin.opex.api.app.proxy.AuthProxy
import co.nilin.opex.api.core.inout.APIKey
import co.nilin.opex.api.core.spi.APIKeyService
import co.nilin.opex.api.ports.postgres.dao.APIKeyRepository
import co.nilin.opex.api.ports.postgres.model.APIKeyModel
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log

@Service
class APIKeyServiceImpl(
    private val apiKeyRepository: APIKeyRepository,
    private val authProxy: AuthProxy,
    private val cacheManager: CacheManager,
    @Value("\${app.auth.api-key-client.secret}")
    private val clientSecret: String
) : APIKeyService {

    private val logger = LoggerFactory.getLogger(APIKeyServiceImpl::class.java)

    override suspend fun createAPIKey(
        userId: String,
        label: String,
        expirationTime: LocalDateTime?,
        allowedIPs: String?,
        currentToken: String
    ): Pair<String, APIKey> {
        if (apiKeyRepository.countByUserId(userId).awaitFirstOrElse { 0 } >= 10)
            throw OpexError.APIKeyLimitReached.exception()

        val secret = generateSecret()
        val tokenResponse = authProxy.exchangeToken(clientSecret, currentToken)
        val apiKey = apiKeyRepository.save(
            APIKeyModel(
                null,
                userId,
                label,
                encryptAES(tokenResponse.access_token, secret),
                encryptAES(tokenResponse.refresh_token, secret),
                expirationTime,
                allowedIPs,
                tokenExpiration(tokenResponse.expires_in)
            )
        ).awaitSingle()

        return Pair(
            secret,
            with(apiKey) {
                APIKey(userId, label, accessToken, expirationTime, allowedIPs, key, isEnabled, isExpired)
            }
        )
    }

    override suspend fun getAPIKey(key: String, secret: String): APIKey? = coroutineScope {
        val apiKey = getFromCache(key)
            ?: apiKeyRepository.findByKey(key).awaitSingleOrNull()?.apply { putCache(this) }

        with(apiKey) {
            if (this != null) {
                launch { checkupAPIKey(this@with, secret) }
                APIKey(
                    userId,
                    label,
                    decryptAES(accessToken, secret),
                    expirationTime,
                    allowedIPs,
                    key,
                    isEnabled,
                    isExpired
                )
            } else
                null
        }
    }

    override suspend fun getKeysByUserId(userId: String): List<APIKey> {
        return apiKeyRepository.findAllByUserId(userId).collectList().awaitFirstOrElse { emptyList() }
            .map {
                APIKey(
                    it.userId,
                    it.label,
                    it.accessToken,
                    it.expirationTime,
                    it.allowedIPs,
                    it.key,
                    it.isEnabled,
                    it.isExpired
                )
            }
    }

    override suspend fun changeKeyState(userId: String, key: String, isEnabled: Boolean) {
        val apiKey = apiKeyRepository.findByKey(key).awaitSingleOrNull() ?: throw OpexError.NotFound.exception()
        if (apiKey.userId != userId)
            throw OpexError.Forbidden.exception()
        apiKey.isEnabled = isEnabled
        apiKeyRepository.save(apiKey).awaitSingle()
    }

    override suspend fun deleteKey(userId: String, key: String) {
        val apiKey = apiKeyRepository.findByKey(key).awaitSingleOrNull() ?: throw OpexError.NotFound.exception()
        if (apiKey.userId != userId)
            throw OpexError.Forbidden.exception()
        apiKeyRepository.delete(apiKey).awaitFirstOrNull()
    }

    private suspend fun checkupAPIKey(apiKey: APIKeyModel, secret: String) {
        if (apiKey.isExpired || !apiKey.isEnabled)
            return

        try {
            val now = LocalDateTime.now()
            if (apiKey.expirationTime?.isBefore(now) == true) {
                logger.info("Expiring api key ${apiKey.key}")
                apiKey.isExpired = true
                apiKeyRepository.save(apiKey).awaitSingle().apply { updateCache(this) }
                logger.info("API key ${apiKey.key} is expired")
                return
            }

            if (apiKey.tokenExpirationTime.isBefore(now)) {
                logger.info("Refreshing api key ${apiKey.key} token")
                val response = authProxy.refreshToken(clientSecret, decryptAES(apiKey.refreshToken, secret))
                apiKey.apply {
                    accessToken = encryptAES(response.access_token, secret)
                    tokenExpirationTime = tokenExpiration(response.expires_in)
                }
                apiKeyRepository.save(apiKey).awaitSingle().apply { updateCache(this) }
                logger.info("API key ${apiKey.key} token refreshed")
            }
        } catch (e: Exception) {
            logger.error("Error checking api key ${apiKey.key}", e)
        }
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

    private fun tokenExpiration(expiresInSeconds: Long): LocalDateTime {
        val tokenOffsetTime = Date().time + TimeUnit.SECONDS.toMillis(expiresInSeconds) - TimeUnit.MINUTES.toMillis(10)
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(tokenOffsetTime), ZoneId.systemDefault())
    }

    private fun getFromCache(key: String): APIKeyModel? {
        return getCache()?.get(key)?.get() as APIKeyModel?
    }

    private fun putCache(apiKey: APIKeyModel) {
        getCache()?.apply {
            putIfAbsent(apiKey.key, apiKey)
        }
    }

    private fun updateCache(apiKey: APIKeyModel) {
        getCache()?.apply {
            evict(apiKey.key)
            put(apiKey.key, apiKey)
        }
    }

    private fun getCache(): Cache? {
        val cache = cacheManager.getCache("apiKey")
        if (cache == null)
            logger.warn("Could not find cache of apiKey")
        return cache
    }

}