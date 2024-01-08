package co.nilin.opex.wallet.ports.proxy.bcgateway.impl

import co.nilin.opex.wallet.core.model.TokenHolder
import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.spi.AuthProxy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.security.Principal
import java.util.function.Function


@Component
class ExtractBackgroundAuth(private val authProxy: AuthProxy, private val environment: Environment) {

    suspend fun extractToken(): String? {
        if (environment.activeProfiles.contains("otc"))
        //todo read using vault
            return authProxy.getToken(LoginRequest("0955555555555", "Pol@Sys204Hg@d5*P")).data.accessToken
        return null
    }
}


