package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.app.service.otc.GraphService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CurrencyRatesConfig {
    @Bean
    fun currencyGraph(): GraphService {
        return GraphService()
    }
}