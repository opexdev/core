package co.nilin.opex.otp.app.config

import co.nilin.opex.otp.app.utils.AudienceValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
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
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/v1/otp/**").permitAll()
            .pathMatchers("/v1/totp/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }


    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        val decoder = NimbusReactiveJwtDecoder.withJwkSetUri(certUrl)
            .webClient(webClient)
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

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
