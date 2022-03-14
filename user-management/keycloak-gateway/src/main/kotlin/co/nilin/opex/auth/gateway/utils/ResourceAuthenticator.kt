package co.nilin.opex.auth.gateway.utils

import org.keycloak.models.KeycloakSession
import org.keycloak.models.UserModel
import org.keycloak.representations.AccessToken
import org.keycloak.services.managers.AppAuthManager
import org.keycloak.services.managers.AuthenticationManager

class ResourceAuthenticator(private val result: AuthenticationManager.AuthResult?) {

    private val user: UserModel? = result?.user
    private val token: AccessToken? = result?.token

    fun getUserId() = user?.id

    fun hasAccess(scope: String, userId: String? = null): Boolean {
        return hasScopeAccess(scope) && hasUserAccess(userId)
    }

    fun hasScopeAccess(scope: String): Boolean {
        if (token == null) return false
        return token.scope.split(" ").contains(scope)
    }

    fun hasUserAccess(userId: String? = null): Boolean {
        if (user == null) return false
        return userId != null && user.id == userId
    }

    companion object {
        fun bearerAuth(session: KeycloakSession): ResourceAuthenticator {
            return ResourceAuthenticator(AppAuthManager.BearerTokenAuthenticator(session).authenticate())
        }
    }

}