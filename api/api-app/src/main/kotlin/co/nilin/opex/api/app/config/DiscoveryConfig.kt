package co.nilin.opex.api.app.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.reactive.WebFluxEndpointManagementContextConfiguration
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
@AutoConfigureBefore(WebFluxEndpointManagementContextConfiguration::class)
class DiscoveryConfig {

    @Bean(name = ["management.endpoints.web.discoveryHandlerMapping"])
    fun disableDiscoveryHandlerMapping(): SimpleUrlHandlerMapping {
        val mapping = SimpleUrlHandlerMapping()
        mapping.order = Int.MAX_VALUE
        mapping.urlMap = emptyMap<String, Any>()
        return mapping
    }
}