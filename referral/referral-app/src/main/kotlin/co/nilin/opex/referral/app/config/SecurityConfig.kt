package co.nilin.opex.referral.app.config

import net.minidev.json.JSONArray
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
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
            .pathMatchers("/codes", "/checkouts", "/commissions/**").access { mono, _ ->
                mono.map { auth ->
                    auth.authorities.any { authority -> authority.authority == "SCOPE_trust" } && ((auth.principal as Jwt).claims["groups"] as JSONArray)
                        .contains("finance-admin")
                }.map { granted ->
                    AuthorizationDecision(granted)
                }
            }
            .pathMatchers("/**").hasAuthority("SCOPE_trust")
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
