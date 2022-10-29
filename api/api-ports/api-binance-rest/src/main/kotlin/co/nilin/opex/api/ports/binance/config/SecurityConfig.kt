package co.nilin.opex.api.ports.binance.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.WebFilter

@EnableWebFluxSecurity
class SecurityConfig(private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    @Autowired
    private lateinit var apiKeyFilter: APIKeyFilter

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-resources/**").permitAll()
            .pathMatchers("/v2/api-docs").permitAll()
            .pathMatchers("/v3/depth").permitAll()
            .pathMatchers("/v3/trades").permitAll()
            .pathMatchers("/v3/ticker/**").permitAll()
            .pathMatchers("/v3/exchangeInfo").permitAll()
            .pathMatchers("/v3/currencyInfo/**").permitAll()
            .pathMatchers("/v3/klines").permitAll()
            .pathMatchers("/socket").permitAll()
            .pathMatchers("/v1/landing/**").permitAll()
            .pathMatchers("/**").hasAuthority("SCOPE_trust")
            .anyExchange().authenticated()
            .and()
            .addFilterBefore(apiKeyFilter as WebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkUrl)
            .webClient(webClient)
            .build()
    }
}
