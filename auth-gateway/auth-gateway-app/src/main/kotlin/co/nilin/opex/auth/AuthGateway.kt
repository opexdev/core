package co.nilin.opex.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
class AuthGateway

fun main(args: Array<String>) {
    runApplication<AuthGateway>(*args)
} 