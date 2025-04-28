package co.nilin.opex.auth.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient

@EnableWebFluxSecurity
@Configuration
class SecurityConfig(
    @Qualifier("keycloakWebClient")
    private val webClient: WebClient,
    private val keycloakConfig: KeycloakConfig
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/**").permitAll()
                it.pathMatchers("/api/v1/oauth/protocol/openid-connect/**").permitAll()
                    .pathMatchers("/api/v1/oauth.***").permitAll()
                    .pathMatchers("/api/v1/users/register**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
    }

    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(keycloakConfig.certUrl)
            .webClient(webClient)
            .build()
    }
}