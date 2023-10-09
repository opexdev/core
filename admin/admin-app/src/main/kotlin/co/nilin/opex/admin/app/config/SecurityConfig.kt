package co.nilin.opex.admin.app.config

import co.nilin.opex.admin.app.utils.hasRole
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient


@EnableWebFluxSecurity
class SecurityConfig(private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/auth/**").hasRole("SCOPE_trust", "admin_finance")
            .pathMatchers("/system/**").hasRole("SCOPE_trust", "admin_system")
            .pathMatchers("/admin/**").hasRole("SCOPE_trust", "admin_system")
            .pathMatchers("/blockchain/**").hasRole("SCOPE_trust", "admin_system")
            .pathMatchers("/actuator/health").permitAll()
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
