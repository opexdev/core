package co.nilin.opex.bcgateway.app.config

import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.zalando.logbook.Logbook
import org.zalando.logbook.netty.LogbookClientHandler
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean
    @Profile("!otc")
    fun loadBalancedWebClient(
        loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>,
        logbook: Logbook
    ): WebClient {
        val client = HttpClient.create().doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }
        return WebClient.builder()
            //.clientConnector(ReactorClientHttpConnector(client))
            .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(20 * 1024 * 1024) }
                    .build()
            )
            .build()
    }

    @Bean
    @Profile("otc")
    fun webClient(logbook: Logbook): WebClient {
        val client = HttpClient.create().doOnConnected { it.addHandlerLast(LogbookClientHandler(logbook)) }
        return WebClient.builder()
            //.clientConnector(ReactorClientHttpConnector(client))
            .exchangeStrategies(
                ExchangeStrategies.builder()
                    .codecs { it.defaultCodecs().maxInMemorySize(20 * 1024 * 1024) }
                    .build()
            )
            .build()
    }
}
