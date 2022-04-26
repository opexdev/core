package co.nilin.opex.admin.ports.auth.proxy

import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KeycloakProxy(private val webClient: WebClient) {

    private val logger = LoggerFactory.getLogger(KeycloakProxy::class.java)

    @Value("\${app.auth.token-url}")
    private lateinit var tokenUrl: String

    suspend fun impersonate(
        token: String,
        clientId: String,
        clientSecret: String,
        userId: String
    ): String {
        val body = BodyInserters.fromFormData("client_id", clientId)
            .with("client_secret", clientSecret)
            .with("requested_subject", userId)
            .with("subject_token", token)
            .with("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            .with("agent", "opex-admin")

        logger.info("Request token exchange for user $userId and client $clientId")
        return webClient.post()
            .uri(tokenUrl)
            .accept(MediaType.APPLICATION_JSON)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(body)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<String>()
            .awaitSingle()
    }

}