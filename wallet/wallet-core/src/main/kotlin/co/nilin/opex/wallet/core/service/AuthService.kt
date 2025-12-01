package co.nilin.opex.wallet.core.service

import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.model.otc.LoginResponse
import co.nilin.opex.wallet.core.spi.AuthProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component


@Component
class AuthService(
    private val authProxy: AuthProxy,
    private val environment: Environment
) {

    @Value("\${app.auth.client-id}")
    private lateinit var clientId: String

    @Value("\${app.auth.client-secret}")
    private lateinit var clientSecret: String

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var expiresAt: Long = 0L

    suspend fun extractToken(): String? {
        if (environment.activeProfiles.contains("otc")) {
            val now = System.currentTimeMillis()

            if (cachedToken != null && now < expiresAt) {
                return cachedToken!!
            }

            val response: LoginResponse = authProxy.getToken(LoginRequest(clientId, clientSecret))

            val expireInMillis = response.data.expireIn * 1000L

            cachedToken = response.data.accessToken
            expiresAt = now + expireInMillis
            return response.data.accessToken
        }
        return null
    }
}


