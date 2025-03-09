package co.nilin.opex.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    
    @Bean("keycloakWebClient")
    fun keycloakWebClient(builder: WebClient.Builder, keycloakConfig: KeycloakConfig): WebClient {
        return builder
            .baseUrl(keycloakConfig.url)
            .build()
    }

    @Bean("otpWebClient")
    fun otpWebClient(builder: WebClient.Builder, otpConfig: OTPConfig): WebClient {
        return builder
            .baseUrl(otpConfig.url)
            .build()
    }
} 