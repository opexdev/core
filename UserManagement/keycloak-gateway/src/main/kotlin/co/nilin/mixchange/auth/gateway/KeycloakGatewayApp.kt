package co.nilin.mixchange.auth.gateway

import co.nilin.mixchange.auth.gateway.config.KeycloakServerProperties
import co.nilin.mixchange.auth.gateway.config.SimplePlatformProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ClassPathResource

@SpringBootApplication(exclude = [LiquibaseAutoConfiguration::class])
@ComponentScan(basePackages = arrayOf("co.nilin.mixchange.auth.gateway"))
@EnableConfigurationProperties
class KeycloakGatewayApp

fun main(args: Array<String>) {
    ApplicationContextHolder.setCurrentContext(runApplication<KeycloakGatewayApp>(*args))
}