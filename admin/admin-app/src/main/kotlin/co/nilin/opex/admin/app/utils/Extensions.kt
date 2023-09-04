package co.nilin.opex.admin.app.utils

import co.nilin.opex.admin.app.config.WebClientConfig
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.shaded.json.JSONArray
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRole(
        authority: String,
        role: String
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        val hasAuthority = auth.authorities.any { it.authority == authority }
        val hasRole = ((auth.principal as Jwt).claims["roles"] as JSONArray?)?.contains(role) == true
        ReactiveSecurityContextHolder.getContext().map { s->s.authentication=auth }
        SecurityContextHolder.getContext().authentication=auth
        val logger = LoggerFactory.getLogger(WebClientConfig::class.java)
        AuthorizationDecision(hasAuthority && hasRole)
    }

}



