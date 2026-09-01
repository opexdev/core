package co.nilin.opex.price.ports.proxy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class PriceProxyWebClientConfig {

    @Bean
    fun priceProxyWebClient(
        @Value("\${app.price-proxy.url}") baseUrl: String,
        @Value("\${app.price-proxy.api-key}") apiKey: String
    ): WebClient {
        // Built standalone (not from an injected WebClient.Builder) so this stays independent of
        // any other WebClient.Builder bean in the app — e.g. the @LoadBalanced one used for wallet
        // sync, which would otherwise silently become the only candidate Spring Boot auto-wires
        // here (its own default builder is @ConditionalOnMissingBean) and wrap this plain HTTP
        // client in a load-balancer filter it has no business going through.
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("X-API-KEY", apiKey)
            .build()
    }
}
