package co.nilin.opex.eventlog.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ComponentScan("co.nilin.opex")
@EnableScheduling
class EventLogApp

fun main(args: Array<String>) {
    runApplication<EventLogApp>(*args)
}