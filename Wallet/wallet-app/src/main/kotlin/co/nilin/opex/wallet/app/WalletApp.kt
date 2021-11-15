package co.nilin.opex.wallet.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableSwagger2
@EnableOpexErrorHandler
class WalletApp

fun main(args: Array<String>) {
    runApplication<WalletApp>(*args)
}
