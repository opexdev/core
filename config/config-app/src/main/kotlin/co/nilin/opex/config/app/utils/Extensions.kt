package co.nilin.opex.config.app.utils

import org.json.JSONArray
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRole(
    authority: String,
    role: String
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        val hasAuthority = auth.authorities.any { it.authority == authority }
        val hasRole = ((auth.principal as Jwt).claims["roles"] as JSONArray?)?.contains(role) == true
        AuthorizationDecision(hasAuthority && hasRole)
    }
}