package co.nilin.opex.auth.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    @Bean("keycloakWebClient")
    fun keycloakWebClient(keycloakConfig: KeycloakConfig, logbook: Logbook): WebClient {
        val provider = ConnectionProvider.builder("keycloakPool")
            .maxConnections(100)
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(2))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofMinutes(1))
            .build()

        val client = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(10))
            .keepAlive(true)
            .doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }

        client.warmup().block()

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

    @LoadBalanced
    @Bean("deviceManagementWebclientBuilder")
    fun deviceManagementWebClientBuilder(deviceManagement: DeviceManagementConfig): WebClient.Builder {
        return WebClient.builder().baseUrl(deviceManagement.url)
    }


    @Bean("deviceManagementClient")
    fun deviceManagementWebClient(@Qualifier("deviceManagementWebclientBuilder") builder: WebClient.Builder): WebClient {
        return builder.build()
    }


} 