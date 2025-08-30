package co.nilin.opex.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClient {
    @Bean("configWebClient")
    fun configWebClient(): WebClient {
        return WebClient.builder()
                .baseUrl("http://opex-config:8080")
                .build()
    }
}