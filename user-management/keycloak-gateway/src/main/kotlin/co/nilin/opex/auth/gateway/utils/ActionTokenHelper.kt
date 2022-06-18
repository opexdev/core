package co.nilin.opex.auth.gateway.utils

import org.keycloak.authentication.actiontoken.execactions.ExecuteActionsActionToken
import org.keycloak.common.util.Time
import org.keycloak.models.Constants
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.keycloak.services.resources.LoginActionsService
import javax.ws.rs.core.UriBuilder

object ActionTokenHelper {

    fun createInternalEmailLink(
        session: KeycloakSession,
        realm: RealmModel,
        token: String
    ): String {
        return LoginActionsService.actionTokenProcessor(session.context.uri)
            .queryParam("key", token)
            .build(realm.name)
            .toString()
    }

    fun attachTokenToLink(link: String, token: String, paramKey: String = "key"): String {
        return UriBuilder.fromUri(link).queryParam(paramKey, token).build().toString()
    }

    fun generateRequiredActionsToken(
        session: KeycloakSession,
        realm: RealmModel,
        user: UserModel,
        actions: List<String>,
        redirectUrl: String? = null
    ): String {
        val lifespan = realm.actionTokenGeneratedByAdminLifespan
        val expiration = Time.currentTime() + lifespan
        val clientId = Constants.ACCOUNT_MANAGEMENT_CLIENT_ID
        val token = ExecuteActionsActionToken(user.id, expiration, actions, redirectUrl, clientId)
        return token.serialize(session, realm, session.context.uri)
    }

}