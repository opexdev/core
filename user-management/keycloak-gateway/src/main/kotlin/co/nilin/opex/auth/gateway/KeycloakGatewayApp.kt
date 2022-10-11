package co.nilin.opex.auth.gateway

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(exclude = [LiquibaseAutoConfiguration::class])
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
@EnableConfigurationProperties
class KeycloakGatewayApp

fun main(args: Array<String>) {
    ApplicationContextHolder.setCurrentContext(runApplication<KeycloakGatewayApp>(*args))
}
