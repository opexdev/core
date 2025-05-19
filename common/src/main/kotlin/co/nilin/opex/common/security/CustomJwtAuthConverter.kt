package co.nilin.opex.common.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class CustomJwtAuthConverter : JwtAuthenticationConverter() {

    override fun extractAuthorities(jwt: Jwt): MutableCollection<GrantedAuthority> {
        val authorities = JwtGrantedAuthoritiesConverter().convert(jwt)
        val permissions = jwt.getClaimAsStringList("permissions")
        if (permissions != null && permissions.isNotEmpty())
            authorities?.addAll(permissions.map { SimpleGrantedAuthority("PERM_${it}") })
        return authorities ?: super.extractAuthorities(jwt)
    }
}