package co.nilin.opex.api.ports.binance.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import co.nilin.opex.api.ports.binance.util.AudienceValidator
import co.nilin.opex.common.security.ReactiveCustomJwtConverter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.WebFilter

@EnableWebFluxSecurity
@Configuration("binanceSecurityConfig")
class SecurityConfig(
    private val apiKeyFilter: APIKeyFilter,
    @Value("\${app.auth.cert-url}")
    private val certUrl: String,
    @Value("\${app.auth.iss-url}")
    private val issUrl: String,
    @Qualifier("keycloakWebClient") private val webClient: WebClient,
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
            .oauth2ResourceServer {
                it.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(ReactiveCustomJwtConverter())
                }
            }
            .build()
    }

    @Bean
    @Order(1)
    fun preAuthSecurityChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .securityMatcher(
                ServerWebExchangeMatchers.pathMatchers(
                    "/opex/v1/oauth/protocol/openid-connect/token/resend-otp"
                )
            )
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().authenticated() }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtDecoder(preAuthJwtDecoder()) } }
            .build()
    }

    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {

        return http.csrf { it.disable() }
            .authorizeExchange {
                it.pathMatchers("/actuator/**").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .pathMatchers("/v1/rate-limit").hasAuthority("ROLE_admin")
                    .pathMatchers("/v2/api-docs").permitAll()
                    .pathMatchers("/v3/depth").permitAll()
                    .pathMatchers("/v3/trades").permitAll()
                    .pathMatchers("/v3/ticker/**").permitAll()
                    .pathMatchers("/v3/exchangeInfo").permitAll()
                    .pathMatchers("/v3/currencyInfo/**").permitAll()
                    .pathMatchers("/v3/klines").permitAll()
                    .pathMatchers("/socket").permitAll()
                    .pathMatchers("/v1/landing/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/v3/order").hasAuthority("PERM_order:write")
                    .pathMatchers(HttpMethod.DELETE, "/v3/order").hasAuthority("PERM_order:write")

                    // Opex endpoints
                    .pathMatchers("/opex/v1/oauth/protocol/openid-connect/**").permitAll()
                    .pathMatchers("/opex/v1/oauth.***").permitAll()
                    .pathMatchers("/opex/v1/user/public/**").permitAll()
                    .pathMatchers("/opex/v1/user/update/**").permitAll()
                    .pathMatchers("/v1/deposit/webhook").permitAll()
                    .pathMatchers("/opex/v1/admin/transactions/**").hasAnyAuthority("ROLE_monitoring", "ROLE_admin")
                    .pathMatchers("/opex/v1/storage/**").permitAll()
                    .pathMatchers("/opex/v1/web/config/**").permitAll()
                    .pathMatchers("/opex/v1/user-level/config/**").permitAll()
                    .pathMatchers("/opex/v1/user/config/**").authenticated()
                    .pathMatchers("/opex/v1/admin/**").hasAuthority("ROLE_admin")
                    .pathMatchers("/opex/v1/deposit/**").hasAuthority("PERM_deposit:write")
                    .pathMatchers(HttpMethod.POST, "/opex/v1/order").hasAuthority("PERM_order:write")
                    .pathMatchers(HttpMethod.PUT, "/opex/v1/order").hasAuthority("PERM_order:write")
                    .pathMatchers(HttpMethod.POST, "/opex/v1/withdraw").hasAuthority("PERM_withdraw:write")
                    .pathMatchers(HttpMethod.PUT, "/opex/v1/withdraw").hasAuthority("PERM_withdraw:write")
                    .pathMatchers("/opex/v1/voucher").hasAuthority("PERM_voucher:submit")
                    .pathMatchers("/opex/v1/market/**").permitAll()
                    .pathMatchers("/opex/v1/analytics/users-detail-assets").permitAll()
                    .pathMatchers(HttpMethod.GET, "/opex/v1/market/chain").permitAll()
                    .pathMatchers(HttpMethod.POST, "/v1/api-key").authenticated()
                    .pathMatchers("/v1/api-key").hasAuthority("ROLE_admin")
                    .pathMatchers(HttpMethod.PUT, "/opex/v1/otc/rate").hasAnyAuthority("ROLE_admin", "ROLE_rate_bot")
                    .pathMatchers(HttpMethod.GET, "/opex/v1/otc/**").permitAll()
                    .pathMatchers("/opex/v1/otc/**").hasAuthority("ROLE_admin")
                    .pathMatchers(HttpMethod.GET, "/opex/v1/bank-account").authenticated()
                    .pathMatchers("/opex/v1/bank-account/**").hasAuthority("PERM_bank_account:write")
                    .anyExchange().authenticated()
            }
            .addFilterBefore(apiKeyFilter as WebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer { it.jwt { jwt -> jwt.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) } }
            .build()
    }

    @Bean
    @Throws(Exception::class)
    @Primary
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

    @Bean("preAuthJwtDecoder")
    @Throws(Exception::class)
    fun preAuthJwtDecoder(): ReactiveJwtDecoder? {
        val decoder = NimbusReactiveJwtDecoder.withJwkSetUri(certUrl)
            .webClient(webClient)
            .build()
        val issuerValidator = JwtValidators.createDefaultWithIssuer(issUrl)
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
