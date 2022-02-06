package co.nilin.opex.auth.gateway.extension

import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.keycloak.models.KeycloakSession
import org.keycloak.models.UserModel
import org.keycloak.representations.AccessToken
import org.keycloak.services.managers.AppAuthManager
import org.keycloak.services.managers.AuthenticationManager

class ResourceAuthenticator(private val result: AuthenticationManager.AuthResult) {

    private val user: UserModel? = result.user
    private val token: AccessToken = result.token

    fun getUserId() = user?.id

    fun checkAccess(scope: String, userId: String? = null) {
        checkScopeAccess(scope)
        checkUserAccess(userId)
    }

    fun checkScopeAccess(scope: String) {
        val scopeAccess = token.scope.split(" ").contains(scope)
        if (!scopeAccess)
            throw OpexException(OpexError.Forbidden)
    }

    fun checkUserAccess(userId: String? = null) {
        val userAccess = userId != null && user?.id == userId
        if (!userAccess)
            throw OpexException(OpexError.Forbidden)
    }

    companion object {
        fun bearerAuth(session: KeycloakSession): ResourceAuthenticator {
            return ResourceAuthenticator(AppAuthManager.BearerTokenAuthenticator(session).authenticate())
        }
    }

}