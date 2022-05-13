package co.nilin.opex.matching.engine.app.config

import co.nilin.opex.utility.preferences.ProjectPreferences
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SetupPreferences() {
    @Autowired
    private lateinit var preferences: ProjectPreferences

    @Bean("symbols")
    fun getSymbols(): List<String> {
        return preferences.markets.map { it.pair ?: "${it.leftSide}_${it.rightSide}" }.map { it.lowercase() }
    }
}
