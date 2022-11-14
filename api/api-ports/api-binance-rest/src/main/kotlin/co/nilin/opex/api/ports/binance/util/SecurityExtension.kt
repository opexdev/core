package co.nilin.opex.api.ports.binance.util

import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun SecurityContext.jwtAuthentication(): JwtAuthenticationToken {
    return authentication as JwtAuthenticationToken
}

fun JwtAuthenticationToken.tokenValue(): String {
    return this.token.tokenValue
}

fun JwtAuthenticationToken.roles(): List<String> {
    val list = arrayListOf<String>()
    (token.claims["roles"] as JSONArray?)?.forEach { list.add(it as String) }
    return list
}