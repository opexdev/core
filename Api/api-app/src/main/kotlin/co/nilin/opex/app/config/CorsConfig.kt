package co.nilin.opex.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class CorsConfig : WebFluxConfigurer {

    @Value("\${app.cors.allowed-hosts}")
    private lateinit var hosts: Array<String>

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*hosts)
            .allowedHeaders("*")
    }

}