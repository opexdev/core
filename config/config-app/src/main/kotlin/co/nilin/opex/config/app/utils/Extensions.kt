package co.nilin.opex.config.app.utils

import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.oauth2.jwt.Jwt

fun <T : HttpSecurityBuilder<T>> AuthorizeHttpRequestsConfigurer<T>.AuthorizedUrl.hasRole(
    authority: String,
    role: String
): AuthorizeHttpRequestsConfigurer<T>.AuthorizationManagerRequestMatcherRegistry = access { auth, _ ->
    val hasAuthority = auth.get().authorities.any { it.authority == authority }
    val hasRole = ((auth.get().principal as Jwt).claims["roles"] as ArrayList<*>?)?.contains(role) == true
    AuthorizationDecision(hasAuthority && hasRole)
}