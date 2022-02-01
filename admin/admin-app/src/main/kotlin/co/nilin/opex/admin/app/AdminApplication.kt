package co.nilin.opex.admin.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdminApplication

fun main(args: Array<String>) {
	runApplication<AdminApplication>(*args)
}
