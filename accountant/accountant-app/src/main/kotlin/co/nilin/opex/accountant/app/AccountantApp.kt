package co.nilin.opex.accountant.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
class AccountantApp

fun main(args: Array<String>) {
    runApplication<AccountantApp>(*args)
}