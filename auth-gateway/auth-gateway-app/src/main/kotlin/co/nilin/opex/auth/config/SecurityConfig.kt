package co.nilin.opex.auth.config

import co.nilin.opex.auth.utils.AudienceValidator
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.reactive.function.client.WebClient

@EnableWebFluxSecurity
@Configuration
class SecurityConfig(
    @Qualifier("keycloakWebClient") private val webClient: WebClient,
    private val keycloakConfig: KeycloakConfig,
) {

    @Value("\${swagger.auth.enabled:false}")
    private var swaggerAuthEnabled: Boolean = false

    @Value("\${swagger.auth.authority:ROLE_admin}")
    private lateinit var swaggerAuthority: String

    @Bean
    @Order(0)
    fun swaggerSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val swaggerPaths = arrayOf(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars/**"
        )

        return http
            .securityMatcher(ServerWebExchangeMatchers.pathMatchers(*swaggerPaths))
            .csrf { it.disable() }
            .authorizeExchange {
                if (swaggerAuthEnabled) {
                    it.anyExchange().hasAuthority(swaggerAuthority)
                } else {
                    it.anyExchange().permitAll()
                }
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
            .authorizeExchange { it.anyExchange().authenticated() }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtDecoder(preAuthJwtDecoder()) } }
            .build()
    }

    @Bean
    @Order(2)
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers("/v1/oauth/protocol/openid-connect/**").permitAll()
                    .pathMatchers("/v1/oauth.***").permitAll()
                    .pathMatchers("/v1/user/public/**").permitAll()
                    .pathMatchers("/v1/user/update/**").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
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
