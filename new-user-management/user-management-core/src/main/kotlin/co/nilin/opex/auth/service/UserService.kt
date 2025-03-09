package co.nilin.opex.auth.service

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.model.RegisterUserRequest
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class UserService(@Qualifier("keycloakWebClient") private val keycloakClient: WebClient,
                  @Qualifier("otpWebClient") private val otpClient: WebClient,
                  private val keycloakConfig: KeycloakConfig) {


    suspend fun registerUser(request: RegisterUserRequest) {
        val keycloakUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"

        val token = getAdminAccessToken()
        //TODO validate otp

        val response = keycloakClient.post()
            .uri(keycloakUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to request.username,
                    "email" to request.email,
                    "emailVerified" to true,
                    "firstName" to request.firstName,
                    "lastName" to request.lastName,
                    "enabled" to true,
                    "credentials" to listOf(
                        mapOf(
                            "type" to "password",
                            "value" to request.password,
                            "temporary" to false
                        )
                    ),
                    "attributes" to mapOf(
                        "kycLevel" to "0"
                    )
                )
            )
            .retrieve()
            .onStatus({ it == HttpStatusCode.valueOf(409) }) { response: ClientResponse ->
                throw UserAlreadyExistsException("User with username ${request.username} or email ${request.email} already exists.")
            }
            .toBodilessEntity()
            .awaitSingle() // Await the completion of the request
    }

    private suspend fun getAdminAccessToken(): String {
        val tokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        val response = keycloakClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${keycloakConfig.clientId}&client_secret=${keycloakConfig.clientSecret}&grant_type=client_credentials")
            .retrieve()
            .awaitBody<Map<String, Any>>() // Assuming the response is a JSON object
        return response["access_token"] as String
    }
} 