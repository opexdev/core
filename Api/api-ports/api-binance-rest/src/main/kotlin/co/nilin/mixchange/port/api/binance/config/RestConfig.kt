package co.nilin.mixchange.port.api.binance.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.Formatter
import java.util.*


@Configuration
class RestConfig {
    @Bean
    fun dateFormatter(): Formatter<Date?>? {
        return object : Formatter<Date?> {
            override fun print(date: Date, locale: Locale): String {
                return date.time.toString()
            }

            override fun parse(date: String, locale: Locale): Date {
                return Date(date.toLong())
            }
        }
    }
}