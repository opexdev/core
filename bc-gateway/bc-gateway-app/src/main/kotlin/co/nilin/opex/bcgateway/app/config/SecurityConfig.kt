package co.nilin.opex.bcgateway.app.config

import co.nilin.opex.bcgateway.app.utils.AudienceValidator
import co.nilin.opex.bcgateway.app.utils.hasRoleAndLevel
import co.nilin.opex.common.security.ReactiveCustomJwtConverter
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
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-resources/**").permitAll()
            .pathMatchers("/wallet-sync/**").permitAll()
            .pathMatchers("/currency/**").permitAll()
            .pathMatchers("/filter/**").authenticated()
            .pathMatchers("/admin/**").hasAuthority("ROLE_admin")
            .pathMatchers("/v1/address/assign").permitAll() //TODO Permission Required ???
            .pathMatchers(HttpMethod.PUT, "/v1/address").hasAuthority("ROLE_admin")
            .pathMatchers("/deposit/**").permitAll()
            .pathMatchers("/addresses/**").hasAuthority("ROLE_admin")
            .pathMatchers("/scanner/**").permitAll()
            .pathMatchers("/crypto-currency/**").permitAll()
            .pathMatchers("/currency/**").permitAll()
            //otc
            .pathMatchers(HttpMethod.PUT, "/v1/address").hasAuthority("ROLE_admin")
            .pathMatchers("/admin/**").hasAuthority("ROLE_admin")
            .pathMatchers("/wallet-sync/**").hasAuthority("ROLE_system")
            .pathMatchers("/crypto-currency/chain").hasAuthority("ROLE_admin")
            .pathMatchers("/crypto-currency/**").hasAuthority("ROLE_system")
            .pathMatchers("/omni-balance/bc/**").hasAuthority("ROLE_admin")
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/scanner/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt { it.jwtAuthenticationConverter(ReactiveCustomJwtConverter()) }
        return http.build()
    }


    @Bean
    @Profile("otc")
    fun otcSpringSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers(HttpMethod.PUT, "/v1/address").hasRoleAndLevel("Admin")
            .pathMatchers("/swagger-resources/**").permitAll()
            .pathMatchers("/admin/**").hasRoleAndLevel("Admin")
            .pathMatchers("/wallet-sync/**").hasRoleAndLevel("System")
            .pathMatchers("/crypto-currency/chain").hasRoleAndLevel("user")
            .pathMatchers("/crypto-currency/**").hasRoleAndLevel("System")
            .pathMatchers("/omni-balance/bc/**").hasRoleAndLevel("Admin")
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/scanner/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
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


    @Bean
    @Profile("otc")
    @Throws(Exception::class)
    fun otcReactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(certUrl)
            .webClient(WebClient.create())
            .build()
    }

}
