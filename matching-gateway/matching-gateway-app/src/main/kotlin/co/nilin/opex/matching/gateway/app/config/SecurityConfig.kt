package co.nilin.opex.matching.gateway.app.config

import co.nilin.opex.common.security.ReactiveCustomJwtConverter
import co.nilin.opex.matching.gateway.app.utils.AudienceValidator
import co.nilin.opex.matching.gateway.app.utils.hasRole
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
class SecurityConfig(private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var certUrl: String
    @Value("\${app.auth.iss-url}")
    private lateinit var issUrl: String
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-resources/**").permitAll()
            .pathMatchers("/v2/api-docs").permitAll()
            .pathMatchers(HttpMethod.GET, "/pair-setting/**").permitAll()
            .pathMatchers(HttpMethod.PUT, "/pair-setting/**").hasAuthority("ROLE_admin")
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt { it.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) }
        return http.build()
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
