package co.nilin.opex.wallet.app.config

import co.nilin.opex.common.security.ReactiveCustomJwtConverter
import co.nilin.opex.wallet.app.utils.AudienceValidator
import co.nilin.opex.wallet.app.utils.hasRoleAndLevel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient

@EnableWebFluxSecurity
@Profile("!test")
class SecurityConfig(private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var certUrl: String
    @Value("\${app.auth.iss-url}")
    private lateinit var issUrl: String

    @Bean
    @Profile("!otc")
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/balanceOf/**").authenticated()
            .pathMatchers("/owner/**").authenticated()
            .pathMatchers("/withdraw").authenticated()
            .pathMatchers(HttpMethod.PUT, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.POST, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.DELETE, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers("/manually/**").hasAuthority("ROLE_admin")
            .pathMatchers("/withdraw/history/**").authenticated()
            .pathMatchers("/withdraw").authenticated()
            .pathMatchers("/withdraw/**").authenticated()
            .pathMatchers("/transaction/**").authenticated()
            .pathMatchers("/admin/v2/transaction/**").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
            .pathMatchers("/admin/withdraw/history").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
            .pathMatchers("/admin/deposit/history").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
            .pathMatchers("/admin/v1/swap/history").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
            .pathMatchers("/admin/**").hasAuthority("ROLE_admin")
            .pathMatchers("/stats/total-assets/**").permitAll()
            .pathMatchers("/stats/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.GET, "/currency/**").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/storage/**").hasAuthority("ROLE_admin")
            .pathMatchers("/deposit/**").permitAll()
            .pathMatchers("/internal/deposit/**").permitAll()
            .pathMatchers("/payment/internal/**").permitAll()
            .pathMatchers("/inquiry/**").permitAll()
            .pathMatchers("/v2/transfer/**").permitAll()
            .pathMatchers("/voucher/**").hasAuthority("PERM_voucher:submit")
            //otc
            .pathMatchers("/admin/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.GET, "/otc/**").permitAll()
            .pathMatchers(HttpMethod.PUT, "/otc/rate").hasAnyAuthority("ROLE_rate_bot","ROLE_admin")
            .pathMatchers(HttpMethod.PUT, "/otc/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.POST, "/otc/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.DELETE, "/otc/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.GET, "/currency/**").permitAll()
            .pathMatchers(HttpMethod.PUT, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.POST, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers(HttpMethod.DELETE, "/currency/**").hasAuthority("ROLE_admin")
            .pathMatchers("/manually/**").hasAuthority("ROLE_admin")
            .pathMatchers("/deposit/**").hasAuthority("ROLE_system")
            .pathMatchers("/internal/deposit/**").hasAuthority("ROLE_system")
            .pathMatchers("/stats/**").hasAuthority("ROLE_admin")
            .pathMatchers("/storage/**").hasAuthority("ROLE_admin")
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt { it.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) }
        return http.build()
    }


    @Bean
    @Profile("otc")
    fun decSpringSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        //todo
        //.pathMatchers("/payment/internal/**").permitAll()

        http.csrf().disable()
            .authorizeExchange()
//                .pathMatchers("/transaction/**").hasAuthority("SCOPE_trust")
            .pathMatchers("/admin/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.PUT, "/otc/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.POST, "/otc/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.DELETE, "/otc/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.PUT, "/currency/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.POST, "/currency/**").hasRoleAndLevel("Admin")
            .pathMatchers(HttpMethod.DELETE, "/currency/**").hasRoleAndLevel("Admin")
            .pathMatchers("/manually/**").hasRoleAndLevel("Admin")
            .pathMatchers("/deposit/**").hasRoleAndLevel("System")
            .pathMatchers("/internal/deposit/**").hasRoleAndLevel("System")
            .pathMatchers("/withdraw/history/**").authenticated()
            .pathMatchers("/withdraw").hasRoleAndLevel("user", "Trusted")
            .pathMatchers("/withdraw/**").hasRoleAndLevel("user", "Trusted")
            .pathMatchers(HttpMethod.GET, "/otc/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/currency/**").permitAll()
            .pathMatchers("/stats/**").hasRoleAndLevel("Admin")
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/storage/**").hasRoleAndLevel("Admin")
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }


    @Bean
    @Profile("otc")
    @Throws(Exception::class)
    fun otcReactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(certUrl)
            .webClient(WebClient.create())
            .build()
    }

    @Bean
    @Profile("!otc")
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        val decoder = NimbusReactiveJwtDecoder.withJwkSetUri(certUrl)
            .webClient(WebClient.create())
            .build()
        val issuerValidator = JwtValidators.createDefaultWithIssuer(issUrl)
        val audienceValidator = AudienceValidator(
            setOf(
                "ios-app",
                "web-app",
                "android-app",
                "opex-api-key"
            )
        )
        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                issuerValidator,
                audienceValidator
            )
        )
        return decoder
    }


}
