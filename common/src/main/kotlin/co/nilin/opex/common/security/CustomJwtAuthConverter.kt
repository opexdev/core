package co.nilin.opex.common.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter
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