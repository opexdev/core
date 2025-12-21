package co.nilin.opex.auth.config

import co.nilin.opex.auth.utils.AudienceValidator
import jakarta.enterprise.inject.Default
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.reactive.function.client.WebClient

@EnableWebFluxSecurity
@Configuration
class SecurityConfig(
    @Qualifier("keycloakWebClient")
    private val webClient: WebClient,
    private val keycloakConfig: KeycloakConfig
) {

    @Bean
    @Order(2)
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.csrf { it.disable() }
            .authorizeExchange {it.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/v1/oauth/protocol/openid-connect/**").permitAll()
                    .pathMatchers("/v1/oauth.***").permitAll()
                    .pathMatchers("/v1/user/public/**").permitAll()
                    .pathMatchers("/v1/user/update/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
            .build()
    }

    @Bean
    @Order(1)
    fun preAuthSecurityChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .securityMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                    "/v1/oauth/protocol/openid-connect/token/resend-otp"
                )
            )
            .csrf { it.disable() }
            .authorizeExchange {
                it.anyExchange().authenticated()
            }
            .oauth2ResourceServer { it ->
                it.jwt {
                    it.jwtDecoder(preAuthJwtDecoder())
                }
            }
            .build()
    }


    @Bean
    @Throws(Exception::class)
    @Primary
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        val decoder = NimbusReactiveJwtDecoder.withJwkSetUri(keycloakConfig.certUrl)
            .webClient(webClient)
            .build()
        val issuerValidator = JwtValidators.createDefaultWithIssuer(keycloakConfig.issUrl)
        val audienceValidator = AudienceValidator(
            setOf(
                "ios-app",
                "web-app",
                "android-app",
                "opex-api-key",
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


    @Bean("preAuthJwtDecoder")
    @Throws(Exception::class)
    fun preAuthJwtDecoder(): ReactiveJwtDecoder? {
        val decoder = NimbusReactiveJwtDecoder.withJwkSetUri(keycloakConfig.certUrl)
            .webClient(webClient)
            .build()
        val issuerValidator = JwtValidators.createDefaultWithIssuer(keycloakConfig.issUrl)
        val audienceValidator = AudienceValidator(
            setOf(
                "pre-auth-client",
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


