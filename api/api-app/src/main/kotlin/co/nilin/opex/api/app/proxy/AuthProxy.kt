package co.nilin.opex.api.app.proxy

import co.nilin.opex.api.app.data.AccessTokenResponse
import co.nilin.opex.common.OpexError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class AuthProxy(
    @Value("\${app.auth.token-url}")
    private val tokenUrl: String
) {

    private val logger = LoggerFactory.getLogger(AuthProxy::class.java)
    private val restTemplate = RestTemplate(
        SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(100000)
            setReadTimeout(10000)
        }
    )

    fun exchangeToken(clientSecret: String, token: String): AccessTokenResponse {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("client_id", "opex-api-key")
            add("client_secret", clientSecret)
            add("subject_token", token)
            add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
            add("scope", "offline_access")
        }

        logger.info("Request token exchange for user")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        val response = restTemplate.exchange<AccessTokenResponse>(tokenUrl, HttpMethod.POST, HttpEntity(body, headers))
        return response.body ?: throw OpexError.InternalServerError.exception()
    }

    fun refreshToken(clientSecret: String, refreshToken: String): AccessTokenResponse {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("client_id", "opex-api-key")
            add("client_secret", clientSecret)
            add("refresh_token", refreshToken)
            add("grant_type", "refresh_token")
        }

        logger.info("Refreshing token")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        val response = restTemplate.exchange<AccessTokenResponse>(tokenUrl, HttpMethod.POST, HttpEntity(body, headers))
        return response.body ?: throw OpexError.InternalServerError.exception()
    }
}