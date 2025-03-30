package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.exception.UserAlreadyExistsException
import co.nilin.opex.auth.model.KeycloakUser
import co.nilin.opex.auth.model.Token
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class KeycloakProxy(@Qualifier("keycloakWebClient") private val keycloakClient: WebClient,
                    private val keycloakConfig: KeycloakConfig)
{
   suspend fun getAdminAccessToken(): String {
        val tokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        val response = keycloakClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${keycloakConfig.clientId}&client_secret=${keycloakConfig.clientSecret}&grant_type=client_credentials")
            .retrieve()
            .awaitBody<Token>() // Assuming the response is a JSON object
        return response.accessToken
    }

    suspend fun getToken(username: String, password: String?): Token{
        val userTokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        return keycloakClient.post()
            .uri(userTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${keycloakConfig.clientId}&client_secret=${keycloakConfig.clientSecret}&grant_type=password&username=${username}&password=${password}")
            .retrieve()
            .awaitBody<Token>()
    }

    suspend fun exchangeGoogleTokenForKeycloakToken(accessToken: String): Token {
        val tokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        val requestBody = "client_id=${keycloakConfig.clientId}&client_secret=${keycloakConfig.clientSecret}&grant_type=urn:ietf:params:oauth:grant-type:token-exchange&subject_token=$accessToken&subject_token_type=urn:ietf:params:oauth:token-type:access_token&subject_issuer=google"
        return keycloakClient.post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono<Token>()
            .awaitSingle()
    }

    suspend fun findUsername(email: String): String {
        // Step 1: Build the URL for the Keycloak Admin REST API
        val userSearchUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users?email=${email}"

        // Step 2: Make a GET request to Keycloak's Admin REST API
        val users = keycloakClient.get()
            .uri(userSearchUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAdminAccessToken()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<List<KeycloakUser>>()
            .awaitSingle()

        // Step 3: Check if a user was found
        if (users.isEmpty()) {
            throw IllegalArgumentException("No user found with email: $email")
        }

        // Step 4: Return the username of the first user in the list
        return users[0].id
    }



    suspend fun createExternalIdpUser(email: String, username: String, password: String): String {
        val userUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"
        val userRequest = mapOf(
            "username" to username,
            "email" to email,
            "emailVerified" to true,
            "enabled" to true,
            "credentials" to listOf(
                mapOf(
                    "type" to "password",
                    "value" to password,
                    "temporary" to false
                )
            )
        )

        val response = keycloakClient.post()
            .uri(userUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAdminAccessToken()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userRequest)
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()

        if (response.statusCode.isError) {
            throw RuntimeException("Failed to create user in Keycloak")
        }

        // Return the user ID (you may need to query Keycloak to get the user ID)
        return findUsername(email)
    }

    suspend fun createUser(email: String, username: String, password: String, firstName: String?, lastName: String?) {
        val keycloakUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"

        val token = getAdminAccessToken()

        val response = keycloakClient.post()
            .uri(keycloakUrl)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "username" to username,
                    "email" to email,
                    "emailVerified" to true,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "enabled" to true,
                    "credentials" to listOf(
                        mapOf(
                            "type" to "password",
                            "value" to password,
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
                throw UserAlreadyExistsException(email)
            }
            .toBodilessEntity()
            .awaitSingle() // Await the completion of the request
    }

    suspend fun linkGoogleIdentity(userId: String, email: String, googleUserId: String) {

        val identityUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/$userId/federated-identity/google"
        val identityRequest = mapOf(
            "identityProvider" to "google",
            "userId" to googleUserId, // Use the Google user ID from the token
            "userName" to email // Use the Google email as the username
        )

        val response = keycloakClient.post()
            .uri(identityUrl)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getAdminAccessToken()}")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(identityRequest)
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()

        if (response.statusCode.isError) {
            throw RuntimeException("Failed to link Google identity to Keycloak user")
        }
    }

}