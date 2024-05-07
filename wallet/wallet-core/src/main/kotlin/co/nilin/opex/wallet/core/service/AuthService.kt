package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.spi.AuthProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component


@Component
class AuthService(private val authProxy: AuthProxy,
                  private val environment: Environment) {

    @Value("\${app.auth.client-id}")
    private lateinit var clientId: String

    @Value("\${app.auth.client-secret}")
    private lateinit var clientSecret: String
    suspend fun extractToken(): String? {
        if (environment.activeProfiles.contains("otc"))
            return authProxy.getToken(LoginRequest(clientId, clientSecret)).data.accessToken
        return null
    }
}


