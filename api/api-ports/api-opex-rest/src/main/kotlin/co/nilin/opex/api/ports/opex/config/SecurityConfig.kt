package co.nilin.opex.api.ports.opex.config

import co.nilin.opex.api.core.spi.APIKeyFilter
import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.WebFilter
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@EnableWebFluxSecurity
class SecurityConfig() {

    @Value("\${app.auth.cert-url}")
    private lateinit var jwkUrl: String

    @Autowired
    private lateinit var apiKeyFilter: APIKeyFilter

    @Bean
    @LoadBalanced
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean
    fun webClient(webclientBuilder: WebClient.Builder): WebClient {
        val cp = ConnectionProvider.builder("apiBinanceOpexWebclientConnectionPool")
            .build()
        val client = HttpClient.create(cp)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(10))
        return webclientBuilder.clientConnector(ReactorClientHttpConnector(client)).build()
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/swagger-ui/**").permitAll()
            .pathMatchers("/swagger-resources/**").permitAll()
            .pathMatchers("/v2/api-docs").permitAll()
            .pathMatchers("/v3/depth").permitAll()
            .pathMatchers("/v3/trades").permitAll()
            .pathMatchers("/v3/ticker/**").permitAll()
            .pathMatchers("/v3/exchangeInfo").permitAll()
            .pathMatchers("/v3/currencyInfo/**").permitAll()
            .pathMatchers("/v3/klines").permitAll()
            .pathMatchers("/socket").permitAll()
            .pathMatchers("/v1/landing/**").permitAll()
            .pathMatchers("/**").hasAuthority("SCOPE_trust")
            .anyExchange().authenticated()
            .and()
            .addFilterBefore(apiKeyFilter as WebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(webClient: WebClient): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkUrl)
            .webClient(webClient)
            .build()
    }
}
