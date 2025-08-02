package co.nilin.opex.api.app.proxy

import co.nilin.opex.api.app.data.AccessTokenResponse
import co.nilin.opex.common.OpexError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.reactive.function.BodyInserters

@Component
class AuthProxy(
    @Value("\${app.auth.token-url}")
    private val tokenUrl: String,
    private val restTemplate: RestTemplate
) {

    private val logger = LoggerFactory.getLogger(AuthProxy::class.java)

    fun exchangeToken(clientSecret: String, token: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("subject_token", token)
            .with("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            .with("scope", "offline_access")

        logger.info("Request token exchange for user")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        val response = restTemplate.exchange<AccessTokenResponse>(tokenUrl, HttpMethod.POST, HttpEntity(body, headers))
        return response.body ?: throw OpexError.InternalServerError.exception()
    }

    fun refreshToken(clientSecret: String, refreshToken: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("refresh_token", refreshToken)
            .with("grant_type", "refresh_token")

        logger.info("Refreshing token")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        val response = restTemplate.exchange<AccessTokenResponse>(tokenUrl, HttpMethod.POST, HttpEntity(body, headers))
        return response.body ?: throw OpexError.InternalServerError.exception()
    }
}