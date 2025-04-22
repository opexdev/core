package co.nilin.opex.auth.config

import io.netty.handler.logging.LogLevel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat
import kotlin.math.truncate

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