package co.nilin.opex.auth.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean("keycloakWebClient")
    fun keycloakWebClient(keycloakConfig: KeycloakConfig, logbook: Logbook): WebClient {
        val client = HttpClient.create().doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
            .baseUrl(keycloakConfig.url)
            .build()
    }

    @LoadBalanced
    @Bean("otpWebclientBuilder")
    fun otpWebClientBuilder(otpConfig: OTPConfig): WebClient.Builder {
        return WebClient.builder().baseUrl(otpConfig.url)
    }

    @Bean("otpWebClient")
    fun otpWebClient(@Qualifier("otpWebclientBuilder") builder: WebClient.Builder): WebClient {
        return builder.build()
    }

    @LoadBalanced
    @Bean("captchaWebclientBuilder")
    fun captchaWebClientBuilder(captchaConfig: CaptchaConfig): WebClient.Builder {
        return WebClient.builder().baseUrl(captchaConfig.url)
    }


    @Bean("captchaWebClient")
    fun captchaWebClient(@Qualifier("captchaWebclientBuilder") builder: WebClient.Builder): WebClient {
        return builder.build()
    }

} 