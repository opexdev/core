package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.data.ActiveSession
import co.nilin.opex.auth.data.UserRole
import co.nilin.opex.auth.model.*
import co.nilin.opex.auth.utils.generateRandomID
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class KeycloakProxy(
    @Qualifier("keycloakWebClient")
    private val keycloakClient: WebClient,
    private val keycloakConfig: KeycloakConfig,
    private val opexRealm: RealmResource
) {

    private val adminClient = keycloakConfig.adminClient
    private val logger by LoggerDelegate()

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
        clientSecret: String?
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
            .onStatus({ it == HttpStatus.valueOf(401) }) {
                throw OpexError.InvalidUserCredentials.exception()
            }
            .awaitBody<Token>()
    }

    suspend fun checkUserCredentials(user: KeycloakUser, password: String) {
        keycloakClient.post()
            .uri("${keycloakConfig.url}/realms/${keycloakConfig.realm}/password/validate")
            .header("Content-Type", "application/json")
            .bodyValue(
                object {
                    val userId = user.id
                    val password = password
                }
            ).retrieve()
            .onStatus({ it == HttpStatus.valueOf(401) }) { throw OpexError.InvalidUserCredentials.exception() }
            .awaitBodilessEntity()
    }

    suspend fun refreshUserToken(
        refreshToken: String,
        clientId: String,
        clientSecret: String?
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

    private suspend fun findUserByAttribute(attr: Attribute): List<KeycloakUser> {
        val uri = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users?q=${attr.key}:${attr.value}"

        return keycloakClient.get()
            .uri(uri)
            .withAdminToken()
            .retrieve()
            .bodyToMono<List<KeycloakUser>>()
            .awaitFirstOrElse { emptyList() }
    }

    private suspend fun findUser(uuid: String): UserResource? {
        return opexRealm.users().get(uuid)
    }

    suspend fun createUser(
        username: Username,
        firstName: String?,
        lastName: String?,
        enabled: Boolean
    ) {
        val keycloakUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users"
        val token = getAdminAccessToken()
        val internalID = generateRandomInternalID()
        val response = keycloakClient.post()
            .uri(keycloakUrl)
            .header("Content-Type", "application/json")
            .withAdminToken(token)
            .bodyValue(
                hashMapOf(
                    "username" to internalID,
                    "emailVerified" to enabled,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "enabled" to enabled,
                    "attributes" to hashMapOf(
                        "kycLevel" to "0"
                    ).apply {
                        if (username.type == UsernameType.MOBILE)
                            put("mobile", username.value)
                        put(Attributes.OTP, OTPType.NONE.name)
                    }
                ).apply { if (username.type == UsernameType.EMAIL) put("email", username.value) }
            )
            .retrieve()
            .onStatus({ it == HttpStatus.valueOf(409) }) {
                throw OpexError.UserAlreadyExists.exception()
            }
            .toBodilessEntity()
            .awaitSingle()
    }

    suspend fun confirmCreateUser(user: KeycloakUser, password: String) {
        val keycloakUrl = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/${user.id}"
        val token = getAdminAccessToken()

        keycloakClient.put()
            .uri(keycloakUrl)
            .header("Content-Type", "application/json")
            .withAdminToken(token)
            .bodyValue(
                hashMapOf(
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
            )
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()
    }

    suspend fun assignRole(userId: String, role: UserRole) {
        val roleRepresentation = opexRealm.roles().get(role.keycloakName).toRepresentation()
        val userResource = opexRealm.users().get(userId)
        userResource.roles().realmLevel().add(listOf(roleRepresentation))
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

    suspend fun logout(userId: String) {
        val url = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/${userId}/logout"
        keycloakClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .withAdminToken()
            .retrieve()
            .toBodilessEntity()
            .awaitSingleOrNull()
    }

    suspend fun resetPassword(userId: String, newPassword: String) {
        val url = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/${userId}/reset-password"
        val request = object {
            val type = "password"
            val value = newPassword
            val temporary = false
        }
        keycloakClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .withAdminToken()
            .bodyValue(request)
            .retrieve()
            .toBodilessEntity()
            .awaitSingleOrNull()
    }

    suspend fun fetchActiveSessions(uuid: String, currentSessionId: String): List<ActiveSession> {
        val user = findUser(uuid) ?: throw OpexError.BadRequest.exception()
        val sessions = user.userSessions
        return sessions.map {
            ActiveSession(
                it.id,
                it.username,
                it.userId,
                it.ipAddress,
                it.start,
                it.lastAccess,
                it.clients.values.firstOrNull(),
                it.id == currentSessionId
            )
        }
    }

    suspend fun logoutSession(uuid: String, sessionId: String) {
        val user = findUser(uuid) ?: throw OpexError.BadRequest.exception()
        user.userSessions.find { it.id == sessionId } ?: OpexError.BadRequest.exception()
        callLogout(sessionId)
    }

    suspend fun logoutOthers(uuid: String, currentSessionId: String) {
        val user = findUser(uuid) ?: throw OpexError.BadRequest.exception()
        user.userSessions.forEach {
            if (currentSessionId != it.id)
                callLogout(it.id)
        }
    }

    suspend fun logoutAll(uuid: String) {
        val user = findUser(uuid) ?: throw OpexError.BadRequest.exception()
        //user.userSessions.forEach { opexRealm.deleteSession(it.id, true) }
        user.logout()
    }

    private suspend fun callLogout(sessionId: String) {
        val url = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/sessions/$sessionId"
        keycloakClient.delete()
            .uri(url)
            .withAdminToken()
            .retrieve()
            .toBodilessEntity()
            .awaitSingleOrNull()
    }

    private suspend fun WebClient.RequestHeadersSpec<*>.withAdminToken(token: String? = null): WebClient.RequestHeadersSpec<*> {
        header(HttpHeaders.AUTHORIZATION, "Bearer ${token ?: getAdminAccessToken()}")
        return this
    }

    private suspend fun WebClient.RequestBodySpec.withAdminToken(token: String? = null): WebClient.RequestBodySpec {
        header(HttpHeaders.AUTHORIZATION, "Bearer ${token ?: getAdminAccessToken()}")
        return this
    }

    suspend fun updateUserInfo(
        userId: String,
        newMobile: String? = null,
        newEmail: String? = null,
        firstName: String? = null,
        lastName: String? = null
    ) {
        val updates = mutableMapOf<String, Any>()

        newEmail?.let {
            updates["email"] = it
            updates["emailVerified"] = true
        }

        newMobile?.let {
            updates["attributes"] = mapOf("mobile" to it)
        }

        firstName?.let { updates["firstName"] = it }
        lastName?.let { updates["lastName"] = it }

        if (updates.isNotEmpty()) {
            updateUserFields(userId, updates)
        }
    }

    private suspend fun updateUserFields(userId: String, updates: Map<String, Any>) {
        val url = "${keycloakConfig.url}/admin/realms/${keycloakConfig.realm}/users/$userId"

        val existingUser = keycloakClient.get()
            .uri(url)
            .withAdminToken()
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .awaitSingle()
            .toMutableMap()

        updates.forEach { (key, value) ->
            existingUser[key] = value
        }

        keycloakClient.put()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .withAdminToken()
            .bodyValue(existingUser)
            .retrieve()
            .toBodilessEntity()
            .awaitSingleOrNull()
    }


    private suspend fun generateRandomInternalID(): String {
        var internalId: String;
        var attempts = 0
        do {
            if (attempts >= 10) {
                throw OpexError.InternalIdGenerateFailed.exception()
            }
            internalId = generateRandomID()
            attempts++
        } while (findUserByAttribute(Attribute("username", internalId)).isNotEmpty())
        return internalId

    }
}