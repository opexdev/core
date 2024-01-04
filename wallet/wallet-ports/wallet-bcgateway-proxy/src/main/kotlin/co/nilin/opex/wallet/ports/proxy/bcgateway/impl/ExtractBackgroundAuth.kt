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
            return authProxy.getToken(LoginRequest("0955555555555", "Pol@Sys204Hg@d5*P")).data.accessToken
        return null
    }

}



//save for config Reactive Security context instead of using api


//class ExtractBackgroundAuth {
//    private val logger = LoggerFactory.getLogger(ExtractBackgroundAuth::class.java)
//
////    fun extractToken(): Mono<String>? {
////        logger.info("going to extract auth...........")
////
////        return ReactiveSecurityContextHolder.getContext()
////                .doOnNext { securityContext ->
////                    val authentication = securityContext.authentication
////                    if (authentication is JwtAuthenticationToken) {
////                        val jwtTokenValue = authentication.token.tokenValue
////                        logger.info("JWT Token Value: $jwtTokenValue")
////                    } else {
////                        logger.info("Authentication is not a JwtAuthenticationToken")
////                    }
////                }
////                .map { it.authentication }
////                .filter { it is JwtAuthenticationToken }
////                .map { (it as JwtAuthenticationToken).token.tokenValue }
////                .doOnNext { jwtTokenValue ->
////                    logger.info("JWT Token Value: $jwtTokenValue")
////                }
////
//
//
//
//    fun extractAndPropagateToken(): Mono<String> {
//        return ReactiveSecurityContextHolder.getContext()
//                .flatMap { securityContext ->
//                    // Extract the token from the context
//                    val token = securityContext.context[TokenContextKey] as? String
//
//                    if (token != null) {
//                        // Update the security context with the token
//                        val updatedContext = securityContext
//                                .contextWrite(ContextPayload.create(TokenContextKey, token))
//                        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(updatedContext))
//                                .thenReturn(token)
//                    } else {
//                        // Handle the case where the token is not present in the context
//                        Mono.error(TokenNotFoundException("Token not found in the context"))
//                    }
//                }
//    }
//}
//    class TokenNotFoundException(message: String) : RuntimeException(message)
//
////    }
