package co.nilin.opex.kyc.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableOpexErrorHandler
@EnableAspectJAutoProxy
class KYCApp

fun main(args: Array<String>) {
    runApplication<KYCApp>(*args)
}
