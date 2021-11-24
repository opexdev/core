package co.nilin.opex.bcgateway.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan("co.nilin.opex.bcgateway")
@EnableScheduling
class BCGatewayApp

fun main(args: Array<String>) {
    runApplication<BCGatewayApp>(*args)
}
