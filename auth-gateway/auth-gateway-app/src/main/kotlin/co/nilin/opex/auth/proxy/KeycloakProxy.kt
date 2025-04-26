package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.model.*
import co.nilin.opex.common.OpexError
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class KeycloakProxy(
    @Qualifier("keycloakWebClient") private val keycloakClient: WebClient,
    private val keycloakConfig: KeycloakConfig
) {

    private val adminClient = keycloakConfig.adminClient

    suspend fun getAdminAccessToken(): String {
        val tokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        val response = keycloakClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${adminClient.id}&client_secret=${adminClient.secret}&grant_type=client_credentials")
            .retrieve()
            .awaitBody<Token>() // Assuming the response is a JSON object
        return response.accessToken
    }

    suspend fun getUserToken(
        username: Username,
        password: String?,
        clientId: String,
        clientSecret: String
    ): Token {
        val users = findUserByAttribute(username.asAttribute())
        if (users.isEmpty())
            throw OpexError.UserNotFound.exception()

        val userTokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        return keycloakClient.post()
            .uri(userTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${clientId}&client_secret=${clientSecret}&grant_type=password&username=${users[0].username}&password=${password}")
            .retrieve()
            .awaitBody<Token>()
    }

    suspend fun refreshUserToken(
        refreshToken: String,
        clientId: String,
        clientSecret: String
    ): Token {
        val userTokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        return keycloakClient.post()
            .uri(userTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${clientId}&client_secret=${clientSecret}&grant_type=refresh_token&refresh_token=${refreshToken}")
            .retrieve()
            .awaitBody<Token>()
    }

    suspend fun exchangeGoogleTokenForKeycloakToken(accessToken: String): Token {
        val tokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        val requestBody =
            "client_id=${adminClient.id}&client_secret=${adminClient.secret}&grant_type=urn:ietf:params:oauth:grant-type:token-exchange&subject_token=$accessToken&subject_token_type=urn:ietf:params:oauth:token-type:access_token&subject_issuer=google"
        return keycloakClient.post()
            .uri(tokenUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono<Token>()
            .awaitSingle()
    }

    suspend fun findUserByEmail(email: String): String {
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

    suspend fun findUserByUsername(username: Username): KeycloakUser? {
        val users = findUserByAttribute(username.asAttribute())
        return if (users.isEmpty()) null else users[0]
    }

    suspend fun findUserByAttribute(attr: Attribute): List<KeycloakUser> {
        val uri = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users?q=${attr.key}:${attr.value}"

        return keycloakClient.get()
            .uri(uri)
            .withAdminToken()
            .retrieve()
            .bodyToMono<List<KeycloakUser>>()
            .awaitFirstOrElse { emptyList() }
    }

    suspend fun createExternalIdpUser(email: String, username: Username, password: String): String {
        val userUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"
        val userRequest = mapOf(
            "username" to username.value,
            "email" to email,
            "emailVerified" to true,
            "enabled" to true,
            "credentials" to listOf(
                mapOf(
                    "type" to "password",
                    "value" to password,
                    "temporary" to false
                )
            ),
            "attributes" to hashMapOf(
                "kycLevel" to "0",
                Attributes.OTP to username.type.otpType.name
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
        return findUserByEmail(email)
    }

    suspend fun createUser(
        username: Username,
        password: String,
        firstName: String?,
        lastName: String?
    ) {
        val keycloakUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"
        val token = getAdminAccessToken()

        val response = keycloakClient.post()
            .uri(keycloakUrl)
            .header("Content-Type", "application/json")
            .withAdminToken(token)
            .bodyValue(
                hashMapOf(
                    "username" to username.value,
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
                    "attributes" to hashMapOf(
                        "kycLevel" to "0"
                    ).apply {
                        if (username.type == UsernameType.MOBILE)
                            put("mobile", username.value)
                        put(Attributes.OTP, username.type.otpType.name)
                    }
                ).apply { if (username.type == UsernameType.EMAIL) put("email", username.value) }
            )
            .retrieve()
            .onStatus({ it == HttpStatus.valueOf(409) }) {
                throw OpexError.UserAlreadyExists.exception()
            }
            .toBodilessEntity()
            .awaitSingle() // Await the completion of the request
    }

    suspend fun linkGoogleIdentity(userId: String, email: String, googleUserId: String) {
        val identityUrl =
            "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/$userId/federated-identity/google"
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

    suspend fun WebClient.RequestHeadersSpec<*>.withAdminToken(token: String? = null): WebClient.RequestHeadersSpec<*> {
        header(HttpHeaders.AUTHORIZATION, "Bearer ${token ?: getAdminAccessToken()}")
        return this
    }

    suspend fun WebClient.RequestBodySpec.withAdminToken(token: String? = null): WebClient.RequestBodySpec {
        header(HttpHeaders.AUTHORIZATION, "Bearer ${token ?: getAdminAccessToken()}")
        return this
    }

}