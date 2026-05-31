package co.nilin.opex.api.app.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration(proxyBeanMethods = false)
class CorsConfig(
    @Value("\${app.cors.enabled:false}")
    private val enabled: Boolean,

    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String
) {
    private val logger = LoggerFactory.getLogger(CorsConfig::class.java)

    private val logger = LoggerFactory.getLogger(CorsConfig::class.java)


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun corsWebFilter(): CorsWebFilter {
        val config = CorsConfiguration().apply {
            allowedOrigins = if (enabled) {
                this@CorsConfig.allowedOrigins
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
            } else {
                emptyList()
            }
            allowedOrigins?.forEach {
                logger.info("Allowed origin: {}", it)
            }
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Location", "Content-Disposition")
            allowCredentials = false
            maxAge = 3600
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return CorsWebFilter(source)
    }
}