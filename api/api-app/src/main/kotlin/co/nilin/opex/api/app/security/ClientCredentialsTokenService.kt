package co.nilin.opex.api.app.security

import co.nilin.opex.api.app.proxy.AuthProxy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ClientCredentialsTokenService(
    private val authProxy: AuthProxy,
    @Value("\${app.auth.api-key-client.id}")
    private val clientId: String,
    @Value("\${app.auth.api-key-client.secret}")
    private val clientSecret: String
) {
    private val logger = LoggerFactory.getLogger(ClientCredentialsTokenService::class.java)

    private data class CachedToken(val token: String, val expiresAtMillis: Long)

    // Cache for client_credentials token
    @Volatile private var cache: CachedToken? = null

    // Cache for exchanged user tokens per subject (exactly like getBearerToken, keyed only by subject)
    private val subjectCache = java.util.concurrent.ConcurrentHashMap<String, CachedToken>()

    suspend fun getBearerToken(): String {
        val now = Instant.now().toEpochMilli()
        val snap = cache
        if (snap != null && snap.expiresAtMillis - 30_000 > now) return snap.token
        val resp = authProxy.clientCredentials(clientId, clientSecret)
        val expiresAt = now + (resp.expires_in * 1000L)
        val bearer = resp.access_token
        cache = CachedToken(bearer, expiresAt)
        logger.debug("Fetched new client_credentials token; expires at {}", expiresAt)
        return bearer
    }

    // Convenience: exchange cached client token for a user access token, cached per subject
    suspend fun exchangeToUserToken(requestedSubjectUserId: String, audience: String? = null): String {
        val now = Instant.now().toEpochMilli()
        val cached = subjectCache[requestedSubjectUserId]
        if (cached != null && cached.expiresAtMillis - 30_000 > now) return cached.token

        val subjectToken = getBearerToken()
        val exchanged = authProxy.exchangeToUser(clientId, clientSecret, subjectToken, requestedSubjectUserId, audience)
        val token = exchanged.access_token
        val expiresAt = now + (exchanged.expires_in * 1000L)
        subjectCache[requestedSubjectUserId] = CachedToken(token, expiresAt)
        logger.debug("Exchanged and cached user token for subject {}; expires at {}", requestedSubjectUserId, expiresAt)
        return token
    }
}