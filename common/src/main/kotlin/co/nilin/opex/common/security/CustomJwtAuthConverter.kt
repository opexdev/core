package co.nilin.opex.common.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import reactor.core.publisher.Mono

class ReactiveCustomJwtConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    override fun convert(source: Jwt): Mono<AbstractAuthenticationToken> {
        val permissions = source.getClaimAsStringList("permissions")
            ?.map { SimpleGrantedAuthority("PERM_${it}") }
            ?.toList() ?: emptyList()
        val roles = source.getClaimAsStringList("roles")
            ?.map { SimpleGrantedAuthority("ROLE_${it}") }
            ?.toList() ?: emptyList()
        return Mono.just(JwtAuthenticationToken(source, roles + permissions))
    }
}

class CustomJwtConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(source: Jwt): AbstractAuthenticationToken {
        val permissions = source.getClaimAsStringList("permissions")
            ?.map { SimpleGrantedAuthority("PERM_${it}") }
            ?.toList() ?: emptyList()
        val roles = source.getClaimAsStringList("roles")
            ?.map { SimpleGrantedAuthority("ROLE_${it}") }
            ?.toList() ?: emptyList()
        return JwtAuthenticationToken(source, roles + permissions)
    }
}
