package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.app.utils.hasRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient

@EnableWebFluxSecurity
@Profile("!test")
class SecurityConfig(private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {

        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/balanceOf/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/owner/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/withdraw").hasAuthority("SCOPE_trust")
            .pathMatchers("/withdraw/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/transaction/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/admin/**").hasRole("SCOPE_trust","admin_finance")
            .pathMatchers("/payment/internal/**").permitAll()
            .pathMatchers("/**").permitAll()
            .anyExchange().authenticated()
            .and()
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
