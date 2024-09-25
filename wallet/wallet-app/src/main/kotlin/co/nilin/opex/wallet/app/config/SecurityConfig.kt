package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.app.utils.hasRole
import co.nilin.opex.wallet.app.utils.hasRoleAndLevel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
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
    @Profile("!otc")
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/balanceOf/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/owner/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/withdraw").hasAuthority("SCOPE_trust")
            .pathMatchers("/v1/deposit/manually/**").hasRole("SCOPE_trust", "admin_finance")
            .pathMatchers("/v1/deposit/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/withdraw/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/transaction/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/v2/transaction/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/admin/**").hasRole("SCOPE_trust", "admin_finance")
            .pathMatchers("/stats/**").hasRole("SCOPE_trust", "admin_finance")
            .pathMatchers("/payment/internal/**").permitAll()
            .pathMatchers("/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }

    @Bean
    @Profile("otc")
    fun decSpringSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        //todo
        //.pathMatchers("/payment/internal/**").permitAll()

        http.csrf().disable()
            .authorizeExchange()
            //.pathMatchers("/transaction/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/admin/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.PUT, "/otc/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.POST, "/otc/**").hasRoleAndLevel("Admin")
            .pathMatchers("/manually/**").hasRoleAndLevel("Admin")
            .pathMatchers("/deposit/**").hasRoleAndLevel("System")
            .pathMatchers("/withdraw").hasRoleAndLevel("user", "Trusted")
            .pathMatchers("/withdraw/**").hasRoleAndLevel("user", "Trusted")
            .pathMatchers(HttpMethod.GET, "/otc/**").permitAll()
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
