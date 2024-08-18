package co.nilin.opex.matching.engine.app.config

import co.nilin.opex.utility.preferences.Preferences
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InitializeService {
    @Autowired
    private lateinit var preferences: Preferences

    @Bean("symbols")
    fun getSymbols(): List<String> = preferences.markets.map { it.pair ?: "${it.leftSide}_${it.rightSide}" }
}
