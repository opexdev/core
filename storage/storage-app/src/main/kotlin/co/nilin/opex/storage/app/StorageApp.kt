package co.nilin.opex.storage.app

import co.nilin.opex.utility.error.EnableOpexErrorHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex.storage")
@EnableOpexErrorHandler
class StorageApp

fun main(args: Array<String>) {
    runApplication<StorageApp>(*args)
}
