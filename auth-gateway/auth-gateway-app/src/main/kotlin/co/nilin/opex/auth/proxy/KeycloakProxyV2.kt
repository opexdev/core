package co.nilin.opex.auth.proxy

import co.nilin.opex.auth.config.KeycloakConfig
import co.nilin.opex.auth.model.Attribute
import co.nilin.opex.auth.model.Token
import co.nilin.opex.auth.model.Username
import co.nilin.opex.common.OpexError
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class KeycloakProxyV2(
    private val keycloak: Keycloak,
    private val opexRealm: RealmResource,
    private val keycloakConfig: KeycloakConfig,
    @Qualifier("keycloakWebClient")
    private val client: WebClient
) {

    suspend fun getUserToken(username: Username, password: String, clientId: String, clientSecret: String): Token {
        val user = findByUsername(username) ?: throw OpexError.InvalidUserCredentials.exception()
        return getUserToken(user.username, password, clientId, clientSecret)
    }

    fun findByUsername(username: Username): UserRepresentation? {
        val users = findUserByAttribute(username.asAttribute())
        if (users.isEmpty())
            return null
        return users[0]
    }

    private suspend fun getUserToken(
        username: String,
        password: String?,
        clientId: String,
        clientSecret: String
    ): Token {
        val userTokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        return client.post()
            .uri(userTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${clientId}&client_secret=${clientSecret}&grant_type=password&username=${username}&password=${password}")
            .retrieve()
            .onStatus({ it == HttpStatus.valueOf(401) }) {
                throw OpexError.InvalidUserCredentials.exception()
            }
            .awaitBody<Token>()
    }

    suspend fun refreshUserToken(
        refreshToken: String,
        clientId: String,
        clientSecret: String
    ): Token {
        val userTokenUrl = "${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token"
        return client.post()
            .uri(userTokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .bodyValue("client_id=${clientId}&client_secret=${clientSecret}&grant_type=refresh_token&refresh_token=${refreshToken}")
            .retrieve()
            .onStatus({ it == HttpStatus.valueOf(401) }) {
                throw OpexError.InvalidUserCredentials.exception()
            }
            .awaitBody<Token>()
    }

    private fun findUserByAttribute(attr: Attribute, exact: Boolean = true): List<UserRepresentation> {
        return opexRealm.users().searchByAttributes("${attr.key}:${attr.value}", exact)
    }
}