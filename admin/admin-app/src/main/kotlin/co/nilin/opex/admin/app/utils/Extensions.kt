package co.nilin.opex.admin.app.utils

import com.nimbusds.jose.shaded.json.JSONArray
import com.nimbusds.jose.shaded.json.JSONObject
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRealmRole(
    authority: String,
    role: String
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        auth.authorities.any { it.authority == authority }
                && (((auth.principal as Jwt).claims["realm_access"] as JSONObject?)?.get("roles") as JSONArray?)
            ?.contains(role) == true
    }.map { granted ->
        AuthorizationDecision(granted)
    }
}