package co.nilin.opex.api.app.proxy

import co.nilin.opex.api.app.data.AccessTokenResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class AuthProxy(
    @Value("\${app.auth.token-url}")
    private val tokenUrl: String
) {

    private val logger = LoggerFactory.getLogger(AuthProxy::class.java)
    private val client = WebClient.create()

    suspend fun exchangeToken(clientSecret: String, token: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("subject_token", token)
            .with("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            .with("scope", "offline_access")

        logger.info("Request token exchange for user")
        return client.post()
            .uri(tokenUrl)
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(body)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<AccessTokenResponse>()
            .awaitSingle()
    }

    suspend fun refreshToken(clientSecret: String, refreshToken: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("refresh_token", refreshToken)
            .with("grant_type", "refresh_token")

        logger.info("Refreshing token")
        return client.post()
            .uri(tokenUrl)
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(body)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<AccessTokenResponse>()
            .awaitSingle()
    }
}