package co.nilin.opex.bcgateway.app.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.util.Base64Utils
import org.springframework.util.FileCopyUtils
import org.springframework.web.reactive.function.client.WebClient
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec

@EnableWebFluxSecurity
class SecurityConfig(@Qualifier("loadBalanced") private val webClient: WebClient) {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
                .authorizeExchange()
                .pathMatchers("/filter/**").hasAuthority("SCOPE_trust")
                .pathMatchers("/**").permitAll()
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
