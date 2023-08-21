package co.nilin.opex.profile.app.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    @Qualifier("loadBalanced")
    fun loadBalancedWebClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        return WebClient.builder()
                .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs { it.defaultCodecs().maxInMemorySize(20 * 1024 * 1024) }
                                .build()
                )
                .build()
    }
    @Bean
    fun webClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        return WebClient.builder()
            .filter(
                ReactorLoadBalancerExchangeFilterFunction(
                    loadBalancerFactory, LoadBalancerProperties(), emptyList()
                )
            )
            .build()
    }
}
