package co.nilin.opex.api.ports.opex.config

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    @LoadBalanced
    fun restTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(100000)
            setReadTimeout(10000)
        }

        val restTemplate = RestTemplate(factory)
        restTemplate.errorHandler = DefaultResponseErrorHandler()

        return restTemplate
    }


}