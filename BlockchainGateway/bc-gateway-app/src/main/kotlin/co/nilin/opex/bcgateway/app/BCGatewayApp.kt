package co.nilin.opex.bcgateway.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.mixchange")
class WalletApp

fun main(args: Array<String>) {
	runApplication<WalletApp>(*args)
}
