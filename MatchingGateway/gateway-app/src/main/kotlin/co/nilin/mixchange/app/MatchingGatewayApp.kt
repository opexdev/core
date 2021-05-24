package co.nilin.mixchange.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.mixchange")
class MatchingGatewayApp
fun main(args: Array<String>) {
  runApplication<MatchingGatewayApp>(*args)
}