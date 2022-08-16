package co.nilin.opex.market.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("co.nilin.opex")
class MarketAppApplication

fun main(args: Array<String>) {
	runApplication<MarketAppApplication>(*args)
}
