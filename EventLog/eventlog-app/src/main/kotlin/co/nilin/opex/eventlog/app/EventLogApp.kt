package co.nilin.opex.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
class EventLogApp

fun main(args: Array<String>) {
    runApplication<EventLogApp>(*args)
}