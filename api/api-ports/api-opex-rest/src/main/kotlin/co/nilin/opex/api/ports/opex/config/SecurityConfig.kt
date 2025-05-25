package co.nilin.opex.api.ports.opex.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import co.nilin.opex.common.security.ReactiveCustomJwtConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient

//@EnableWebFluxSecurity
//@EnableMethodSecurity
//@Configuration("opexSecurityConfig")
class SecurityConfig(
    private val apiKeyFilter: APIKeyFilter,
    @Value("\${app.auth.cert-url}")
    private val jwkUrl: String
) {

    //@Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/swagger-ui/**").permitAll()
                    .pathMatchers("/opex/v1/market/**").permitAll()
                    .pathMatchers("/opex/v1/order/**").hasAuthority("PERM_order:write")
                    .pathMatchers("/**").hasAuthority("SCOPE_trust")
                    .anyExchange().authenticated()
            }
//            .addFilterBefore(apiKeyFilter as WebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) } }
            .build()
    }

    //@Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkUrl)
            .webClient(WebClient.builder().build())
            .build()
    }
}
