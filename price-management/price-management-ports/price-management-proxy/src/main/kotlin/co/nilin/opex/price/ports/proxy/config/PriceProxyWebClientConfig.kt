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
        return WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("X-API-KEY", apiKey)
            .build()
    }
}
