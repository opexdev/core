package co.nilin.opex.profile.ports.auth.impl

import co.nilin.opex.profile.core.spi.AuthProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

@Component
class AuthProxyImpl(private val webClient: WebClient) : AuthProxy {

    @Value("\${app.auth-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun updateEmail(userId: String, email: String) {
        webClient.put().uri("$baseUrl/v1/user/update/email")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("userId" to userId, "email" to email))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun updateMobile(userId: String, mobile: String) {
        webClient.put().uri("$baseUrl/v1/user/update/mobile")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("userId" to userId, "mobile" to mobile))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun updateName(userId: String, firstName: String, lastName: String) {
        webClient.put().uri("$baseUrl/v1/user/update/name")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("userId" to userId, "firstName" to firstName, "lastName" to lastName))
            .retrieve()
            .awaitBodilessEntity()
    }
}
