package co.nilin.opex.api.app.config

import co.nilin.opex.common.data.UserLanguage
import co.nilin.opex.common.utils.LanguageUtils.getDefaultUserLanguage
import io.netty.channel.ChannelOption
import io.netty.handler.logging.LogLevel
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
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
import reactor.netty.transport.logging.AdvancedByteBufFormat
import java.time.Duration

@Configuration
class WebClientConfig(private val logbook: Logbook) {
    private val provider = ConnectionProvider.builder("apiPool")
        .maxConnections(150)
        .pendingAcquireMaxCount(100)
        .maxIdleTime(Duration.ofSeconds(30))
        .maxLifeTime(Duration.ofMinutes(2))
        .pendingAcquireTimeout(Duration.ofSeconds(10))
        .evictInBackground(Duration.ofMinutes(1))
        .build()

    private val client = HttpClient.create(provider)
        .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.SIMPLE)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofSeconds(5))
        .keepAlive(true)
        .doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }


    @Bean("generalWebClient")
    fun loadBalancedWebClient(
        loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>,

        ): WebClient {
        return WebClient.builder()
            .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
            .clientConnector(ReactorClientHttpConnector(client))
            .filter(languageFilter())
            .build()
    }

    @Bean("keycloakWebClient")
    fun keycloakWebClient(logbook: Logbook): WebClient {
        val provider = ConnectionProvider.builder("apiKeycloakPool")
            .maxConnections(150)
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofMinutes(2))
            .pendingAcquireTimeout(Duration.ofSeconds(5))
            .evictInBackground(Duration.ofMinutes(1))
            .build()

        val client = HttpClient.create(provider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(5))
            .keepAlive(true)
            .doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }

        client.warmup().block()

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
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