package co.nilin.opex.referral.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
class ReferralApp

fun main(args: Array<String>) {
    runApplication<ReferralApp>(*args)
}