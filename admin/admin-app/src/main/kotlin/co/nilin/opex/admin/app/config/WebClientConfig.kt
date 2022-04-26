package co.nilin.opex.admin.app.config

import co.nilin.opex.utility.log.CustomLogger
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        val logger = CustomLogger(HttpClient::class.java)
        val connector = HttpClient.create().doOnRequest { _, con -> con.addHandlerFirst(logger) }
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(connector))
            .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
            .build()
    }

}
