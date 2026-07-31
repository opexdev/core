package co.nilin.opex.matching.engine.app.config

import co.nilin.opex.matching.engine.core.engine.MatchingEngineRecoveryManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class MatchingEngineInitializer(
    private val recoveryManager: MatchingEngineRecoveryManager
) {
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        recoveryManager.markRunning()
    }

    @Bean("symbols")
    fun getSymbols(@Value("\${app.symbols}") symbols: String): List<String> {
        return symbols.split(",").map { it.trim() }.map { it.uppercase() }
    }
}