package co.nilin.opex.admin.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
class AdminApplication

fun main(args: Array<String>) {
    runApplication<AdminApplication>(*args)
}
