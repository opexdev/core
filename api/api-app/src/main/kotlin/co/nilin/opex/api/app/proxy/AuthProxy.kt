package co.nilin.opex.api.app.proxy

import co.nilin.opex.api.app.data.AccessTokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters

@Component
class AuthProxy(
    @Value("\${app.auth.token-url}")
    private val tokenUrl: String
) {

    private val logger = LoggerFactory.getLogger(AuthProxy::class.java)
    private val restTemplate = RestTemplate()

    suspend fun exchangeToken(clientSecret: String, token: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("subject_token", token)
            .with("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            .with("scope", "offline_access")

        logger.info("Request token exchange for user")
        val headers = org.springframework.http.HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val httpEntity = org.springframework.http.HttpEntity(body, headers)

        val response = restTemplate.postForEntity(
            tokenUrl,
            httpEntity,
            AccessTokenResponse::class.java
        )

        return response.body ?: throw RuntimeException("Failed to get access token")
    }

    suspend fun refreshToken(clientSecret: String, refreshToken: String): AccessTokenResponse {
        val body = BodyInserters.fromFormData("client_id", "opex-api-key")
            .with("client_secret", clientSecret)
            .with("refresh_token", refreshToken)
            .with("grant_type", "refresh_token")

        logger.info("Refreshing token")
        val headers = org.springframework.http.HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val httpEntity = org.springframework.http.HttpEntity(body, headers)

        val response = restTemplate.postForEntity(
            tokenUrl,
            httpEntity,
            AccessTokenResponse::class.java
        )

        return response.body ?: throw RuntimeException("Failed to get access token")
    }
}