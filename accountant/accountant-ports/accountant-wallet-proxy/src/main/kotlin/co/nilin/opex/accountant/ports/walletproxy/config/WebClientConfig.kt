package co.nilin.opex.accountant.ports.walletproxy.config

import io.netty.channel.ChannelOption
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
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

    @Bean
    fun webClient(
        loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>,
        logbook: Logbook
    ): WebClient {

        val connectionProvider = ConnectionProvider.builder("accountant-wallet")
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .pendingAcquireTimeout(Duration.ofSeconds(5))
            .evictInBackground(Duration.ofSeconds(30))
            .lifo()
            .build()

        val client = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(10))
            .keepAlive(true)
            .doOnConnected {
                it.addHandlerLast(LogbookClientHandler(logbook))
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(client))
            .filter(
                ReactorLoadBalancerExchangeFilterFunction(
                    loadBalancerFactory,
                    emptyList()
                )
            )
            .build()
    }
}