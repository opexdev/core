package co.nilin.opex.wallet.app.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    @Profile("!otc")
    fun loadBalancedWebClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        return WebClient.builder()
            .filter(
                ReactorLoadBalancerExchangeFilterFunction(
                    loadBalancerFactory, LoadBalancerProperties(), emptyList()
                )
            )
            .build()
    }

    @Bean
    @Profile("otc")
    @Qualifier("otcWebClient")
    fun webClient(): WebClient {
        return WebClient.builder()
                .build()
    }

}
