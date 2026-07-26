package co.nilin.opex.matching.engine.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InitializeService {

    @Bean("symbols")
    fun getSymbols(@Value("\${app.symbols}") symbols: String): List<String> {
        return symbols.split(",").map { it.trim() }.map { it.uppercase() }
    }
}
