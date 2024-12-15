package co.nilin.opex.auth.gateway

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [LiquibaseAutoConfiguration::class])
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
@EnableConfigurationProperties
@EnableScheduling
@EntityScan(basePackages = ["co.nilin.opex.auth.ports.postgres.model"])
class KeycloakGatewayApp

fun main(args: Array<String>) {
    ApplicationContextHolder.setCurrentContext(runApplication<KeycloakGatewayApp>(*args))
}
