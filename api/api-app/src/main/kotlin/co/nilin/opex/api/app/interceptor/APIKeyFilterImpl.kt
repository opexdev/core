package co.nilin.opex.api.app.interceptor

import co.nilin.opex.api.app.security.ClientCredentialsTokenService
import co.nilin.opex.api.app.security.HmacVerifier
import co.nilin.opex.api.core.spi.APIKeyService
import co.nilin.opex.api.core.spi.APIKeyFilter
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class APIKeyFilterImpl(
    private val apiKeyService: APIKeyService,
    private val hmacVerifier: HmacVerifier,
    private val clientTokenService: ClientCredentialsTokenService
) : APIKeyFilter, WebFilter {

    private val logger = LoggerFactory.getLogger(APIKeyFilterImpl::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        val apiKeyId = request.headers["X-API-KEY"]?.firstOrNull()
        val signature = request.headers["X-API-SIGNATURE"]?.firstOrNull()
        val tsHeader = request.headers["X-API-TIMESTAMP"]?.firstOrNull()
        val uri = request.uri

        // HMAC path when signature present
        if (!apiKeyId.isNullOrBlank() && !signature.isNullOrBlank() && !tsHeader.isNullOrBlank()) {
            return mono {
                val entry = apiKeyService.getApiKeyForVerification(apiKeyId)
                if (entry == null || !entry.enabled) {
                    logger.warn("Unknown or disabled API key: {}", apiKeyId)
                    null
                } else {
                    // Optional IP allowlist
                    val sourceIp = request.remoteAddress?.address?.hostAddress
                    if (!entry.allowedIps.isNullOrEmpty() && (sourceIp == null || !entry.allowedIps!!.contains(sourceIp))) {
                        logger.warn("API key {} request from disallowed IP {}", apiKeyId, sourceIp)
                        null
                    }
                    if (!entry.allowedEndpoints.isNullOrEmpty() && ( !entry.allowedEndpoints!!.contains(uri.rawPath))) {
                        logger.warn("API key {} request to unauthorized resource {}", apiKeyId, uri.rawPath)
                        null
                    } else {
                        val ts = tsHeader.toLongOrNull()
                        val bodyHash = request.headers["X-API-BODY-SHA256"]?.firstOrNull()
                        if (ts == null) {
                            logger.warn("Invalid timestamp header for bot {}", apiKeyId)
                            null
                        } else {
                            val ok = hmacVerifier.verify(
                                entry.secret,
                                signature,
                                HmacVerifier.VerificationInput(
                                    method = request.method.name(),
                                    path = uri.rawPath,
                                    query = uri.rawQuery,
                                    timestampMillis = ts,
                                    bodySha256 = bodyHash
                                )
                            )
                            if (!ok) {
                                logger.warn("Invalid signature for apiKey {}", apiKeyId)
                                null
                            } else {
                                val userId = entry.keycloakUserId
                                if (userId.isNullOrBlank()) {
                                    logger.warn("API key {} has no mapped Keycloak userId; rejecting", apiKeyId)
                                    null
                                } else {
                                    val bearer = clientTokenService.exchangeToUserToken(userId)
                                    val req = request.mutate()
                                        .header("Authorization", "Bearer $bearer")
                                        .build()
                                    exchange.mutate().request(req).build()
                                }
                            }
                        }
                    }
                }
            }.flatMap { updatedExchange ->
                if (updatedExchange != null) chain.filter(updatedExchange) else chain.filter(exchange)
            }
        }

        // Secret-only path with X-API-SECRET (kept as requested). We validate the provided secret
        // against the stored HMAC secret for the apiKey, then proceed to exchange to the mapped user token.
        val legacySecret = request.headers["X-API-SECRET"]?.firstOrNull()
        if (apiKeyId.isNullOrBlank() || legacySecret.isNullOrBlank()) {
            return chain.filter(exchange)
        }
        return mono {
            val entry = apiKeyService.getApiKeyForVerification(apiKeyId)
            if (entry == null || !entry.enabled) {
                logger.warn("Unknown or disabled API key on secret path: {}", apiKeyId)
                null
            } else {
                // Optional IP allowlist
                val sourceIp = request.remoteAddress?.address?.hostAddress
                if (!entry.allowedIps.isNullOrEmpty() && (sourceIp == null || !entry.allowedIps!!.contains(sourceIp))) {
                    logger.warn("API key {} request from disallowed IP {} (secret path)", apiKeyId, sourceIp)
                    null
                } else if (legacySecret != entry.secret) {
                    logger.warn("Invalid X-API-SECRET for apiKey {}", apiKeyId)
                    null
                } else {
                    val userId = entry.keycloakUserId
                    if (userId.isNullOrBlank()) {
                        logger.warn("API key {} has no mapped Keycloak userId; rejecting (secret path)", apiKeyId)
                        null
                    } else {
                        val bearer = clientTokenService.exchangeToUserToken(userId)
                        val req = request.mutate()
                            .header("Authorization", "Bearer $bearer")
                            .build()
                        exchange.mutate().request(req).build()
                    }
                }
            }
        }.flatMap { updatedExchange ->
            if (updatedExchange != null) chain.filter(updatedExchange) else chain.filter(exchange)
        }
    }
}