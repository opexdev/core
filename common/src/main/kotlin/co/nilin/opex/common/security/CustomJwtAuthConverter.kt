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

class CustomJwtAuthConverter : JwtAuthenticationConverter() {

    override fun extractAuthorities(jwt: Jwt): MutableCollection<GrantedAuthority> {
        val authorities = JwtGrantedAuthoritiesConverter().convert(jwt)
        val permissions = jwt.getClaimAsStringList("permissions")
        if (permissions != null && permissions.isNotEmpty())
            authorities?.addAll(permissions.map { SimpleGrantedAuthority("PERM_${it}") })
        return authorities ?: super.extractAuthorities(jwt)
    }
}

class ReactiveCustomJwtConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    override fun convert(source: Jwt): Mono<AbstractAuthenticationToken> {
        val authorities = source.getClaimAsStringList("authorities")
            .map { SimpleGrantedAuthority("PERM_${it}") }
            .toList()
        return Mono.just(JwtAuthenticationToken(source, authorities))
    }
}