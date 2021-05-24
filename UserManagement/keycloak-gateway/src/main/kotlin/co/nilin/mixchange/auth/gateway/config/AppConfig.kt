package co.nilin.mixchange.auth.gateway.config

import co.nilin.mixchange.auth.gateway.KeycloakGatewayApp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    private val LOG: Logger = LoggerFactory.getLogger(KeycloakGatewayApp::class.java)

    @Bean
    fun onApplicationReadyEventListener(
            serverProperties:ServerProperties,
            keycloakServerProperties: KeycloakServerProperties
    ):ApplicationListener<ApplicationReadyEvent>
    {
        return ApplicationListener<ApplicationReadyEvent> { evt ->
                val port = serverProperties.port
                val keycloakContextPath = keycloakServerProperties.contextPath
                LOG.info("Embedded Keycloak started: http://localhost:{}{} to use keycloak", port, keycloakContextPath)
        }
    }
}
