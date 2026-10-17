package co.nilin.opex.auth.config

import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.core.publisher.Mono
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

    @Bean("otpWebClient")
    fun otpWebClient(otpConfig: OTPConfig): WebClient {
        return WebClient.builder()
            .baseUrl(otpConfig.url)
            .filter(languageFilter())
            .build()
    }

    @Bean("captchaWebClient")
    fun captchaWebClient(captchaConfig: CaptchaConfig): WebClient {
        return WebClient.builder()
            .baseUrl(captchaConfig.url)
            .filter(languageFilter())
            .build()
    }

    @Bean("deviceManagementClient")
    fun deviceManagementWebClient(deviceManagement: DeviceManagementConfig): WebClient {
        return WebClient.builder()
            .baseUrl(deviceManagement.url)
            .filter(languageFilter())
            .build()
    }

    private fun languageFilter() = ExchangeFilterFunction { request, next ->
        Mono.deferContextual { ctx ->
            val lang = ctx.getOrDefault("lang", getDefaultUserLanguage())
            val mutatedRequest = ClientRequest.from(request)
                .header("Accept-Language", lang)
                .build()
            next.exchange(mutatedRequest)
        }
    }


} 