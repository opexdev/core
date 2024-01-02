package co.nilin.opex.wallet.app.service.otc

import co.nilin.opex.wallet.core.model.otc.LoginRequest
import co.nilin.opex.wallet.core.spi.AuthProxy
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.cmc.CMCStatus.success
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service


@Service
class AuthService(private val authProxy: AuthProxy) {
    @Autowired
    private val environment: Environment? = null
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String
    fun setBackgroundAuth() {
        runBlocking {
            if (environment?.activeProfiles?.contains("otc") == true) {
                val sysAccessToken = authProxy.getToken(LoginRequest("0955555555555", "Pol@Sys204Hg@d5*P")).data.accessToken
                val jwtDecoder: JwtDecoder = NimbusJwtDecoder
                        .withJwkSetUri(jwkUrl)
                        .build()

                val jwt = jwtDecoder.decode(sysAccessToken)

                ReactiveSecurityContextHolder.getContext()
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(JwtAuthenticationToken(jwt))).subscribe(
                        )

//                val name = ReactiveSecurityContextHolder.getContext()
//                        .map(SecurityContext::getAuthentication)
//                        .map(Authentication::getName)

            }
        }
    }
}