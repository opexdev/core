package co.nilin.opex.admin.app.config

import co.nilin.opex.utility.log.CustomLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.util.function.Consumer


@Configuration
class WebClientConfig {
    private val logger = LoggerFactory.getLogger(WebClientConfig::class.java)

    @Bean
    fun webClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        val logger = CustomLogger(HttpClient::class.java)
        val connector = HttpClient.create().doOnRequest { _, con -> con.addHandlerFirst(logger) }
        return WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(connector))
                .filter(ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory, emptyList()))
                .build()
    }


    @Bean
    @Qualifier("logRequest")
    fun backgroundSecurityWebClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        return WebClient.builder()
                .filter(
                        ReactorLoadBalancerExchangeFilterFunction(
                                loadBalancerFactory, LoadBalancerProperties(), emptyList()
                        )
                )
                .filter(logRequest())
                .build()
    }

    private fun logRequest(): ExchangeFilterFunction {
        return ExchangeFilterFunction.ofRequestProcessor { clientRequest: ClientRequest ->
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url())
            clientRequest.headers().forEach { name: String?, values: List<String?> -> values.forEach(Consumer<String?> { value: String? -> logger.info("{}={}", name, value) }) }
            Mono.just<ClientRequest>(clientRequest)
        }
    }

    @Bean
    fun initializingBean(): InitializingBean? {
        return InitializingBean {
            SecurityContextHolder.setStrategyName(
                    SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
        }
    }


}
