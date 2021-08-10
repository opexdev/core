package co.nilin.opex.port.api.binance.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import reactor.core.publisher.Mono

class AuthenticationConverter : Converter<Jwt, Mono<out AbstractAuthenticationToken>> {

    private val authoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(source: Jwt): Mono<out AbstractAuthenticationToken>? {
        return try {
            Mono.just(
                CustomAuthToken(
                    source.claims[IdTokenClaimNames.SUB] as String?,
                    source.claims["name"] as String?,
                    source.claims["preferred_username"] as String?,
                    source.claims["email"] as String?,
                    source.tokenValue,
                    authoritiesConverter.convert(source) ?: arrayListOf()
                )
            )
        } catch (e: Exception) {
            null
        }
    }
}