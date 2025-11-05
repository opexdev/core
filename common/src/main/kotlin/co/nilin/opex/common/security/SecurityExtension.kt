package co.nilin.opex.common.security

import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun SecurityContext.jwtAuthentication(): JwtAuthenticationToken {
    return authentication as JwtAuthenticationToken
}

fun JwtAuthenticationToken.tokenValue(): String {
    return (this.principal as Jwt).tokenValue
}