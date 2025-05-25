package co.nilin.opex.auth.utils

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun SecurityContext.jwtAuthentication(): JwtAuthenticationToken {
    return authentication as JwtAuthenticationToken
}

fun JwtAuthenticationToken.tokenValue(): String {
    return this.token.tokenValue
}