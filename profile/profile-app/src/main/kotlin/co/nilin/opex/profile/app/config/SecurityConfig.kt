package co.nilin.opex.profile.app.config

import co.nilin.opex.common.security.ReactiveCustomJwtConverter
import co.nilin.opex.profile.app.utils.AudienceValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
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
class SecurityConfig {

    @Value("\${app.auth.cert-url}")
    private lateinit var certUrl: String
    @Value("\${app.auth.iss-url}")
    private lateinit var issUrl: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http.csrf { it.disable() }
            .authorizeExchange() {
                it.pathMatchers(HttpMethod.GET, "/admin/**").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
                    .pathMatchers("/admin/**").hasAuthority("ROLE_admin")
                    .pathMatchers(HttpMethod.GET,"/bank-account").permitAll()
                    .pathMatchers("/bank-account/**").hasAuthority("PERM_bank_account:write")
                    .pathMatchers("/actuator/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) } }
            .build()
    }


    @Bean
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
