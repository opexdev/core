package co.nilin.opex.wallet.app.utils

import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

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

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRoleAndLevel(
    role: String? = null,
    level: String? = null
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        val hasLevel = level?.let { ((auth.principal as Jwt).claims["level"] as String?)?.equals(level) == true }
            ?: true
        val hasRole = ((auth.principal as Jwt).claims["roles"] as JSONArray?)?.contains(role) == true
        AuthorizationDecision(hasLevel && hasRole)
    }
}
fun LocalDateTime.asDate(): Date {
    return Date.from(atZone(ZoneId.systemDefault()).toInstant())
}

fun Date.asLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
}

fun Long.asLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Date(this).toInstant(), ZoneId.systemDefault())
}