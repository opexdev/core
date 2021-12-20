package co.nilin.opex.referral.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
class ReferralApp

fun main(args: Array<String>) {
    runApplication<ReferralApp>(*args)
}