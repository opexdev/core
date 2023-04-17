package co.nilin.opex.captcha.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
@EnableScheduling
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
