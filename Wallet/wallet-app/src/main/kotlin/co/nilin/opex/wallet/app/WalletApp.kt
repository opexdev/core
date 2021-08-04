package co.nilin.opex.wallet.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
class WalletApp

fun main(args: Array<String>) {
	runApplication<WalletApp>(*args)
}
