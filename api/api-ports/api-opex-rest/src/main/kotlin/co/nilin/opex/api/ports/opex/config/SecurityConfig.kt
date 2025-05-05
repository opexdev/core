package co.nilin.opex.api.ports.opex.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.WebFilter

@EnableWebFluxSecurity
@Configuration("opexSecurityConfig")
class SecurityConfig(
    private val webClient: WebClient,
    private val apiKeyFilter: APIKeyFilter,
    @Value("\${app.auth.cert-url}")
    private val jwkUrl: String
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/swagger-ui/**").permitAll()
                    .pathMatchers("/opex/v1/market/**").permitAll()
                    .pathMatchers("/**").hasAuthority("SCOPE_trust")
                    .anyExchange().authenticated()
            }
//            .addFilterBefore(apiKeyFilter as WebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
    }

    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkUrl)
            .webClient(webClient)
            .build()
    }
}
