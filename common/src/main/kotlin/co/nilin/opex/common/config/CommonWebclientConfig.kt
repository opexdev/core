package co.nilin.opex.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class CommonWebclientConfig {
    @Bean("CommonWebClient")
    @ConditionalOnMissingBean
    fun commonWebClient(): CommonWebClient {
        return CommonWebClient(WebClient.builder()
                .build())
    }
}
class CommonWebClient(val delegate: WebClient)
