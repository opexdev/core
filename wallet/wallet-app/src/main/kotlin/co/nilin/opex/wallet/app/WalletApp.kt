package co.nilin.opex.wallet.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
@EnableScheduling
@EntityScan()
class WalletApp

fun main(args: Array<String>) {
    runApplication<WalletApp>(*args)
}
