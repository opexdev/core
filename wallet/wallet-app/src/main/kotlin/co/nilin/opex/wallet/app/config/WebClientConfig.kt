package co.nilin.opex.wallet.app.config

import io.netty.channel.ChannelOption
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig(logbook: Logbook) {

    private val provider = ConnectionProvider.builder("walletPool")
        .maxConnections(100)
        .maxIdleTime(Duration.ofSeconds(30))
        .maxLifeTime(Duration.ofMinutes(2))
        .pendingAcquireTimeout(Duration.ofSeconds(60))
        .evictInBackground(Duration.ofMinutes(1))
        .build()

    private val client = HttpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
        .responseTimeout(Duration.ofSeconds(10))
        .keepAlive(true)
        .doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }


    @Bean
    @Profile("!otc")
    @Qualifier("loadBalanced")
    fun loadBalancedWebClient(
        loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>,

        ): WebClient {
        return WebClient.builder()
            .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
            .clientConnector(ReactorClientHttpConnector(client))
            .build()
    }

    @Bean
    @Profile("otc")
    @Qualifier("decWebClient")
    fun webClient(): WebClient {
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
            .build()
    }

}
