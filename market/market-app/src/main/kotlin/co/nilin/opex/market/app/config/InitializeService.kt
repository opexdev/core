package co.nilin.opex.market.app.config

import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.utility.preferences.Preferences
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.annotation.PostConstruct

@Component
@DependsOn("postgresConfig")
class InitializeService(private val rateRepository: CurrencyRateRepository) {

    @Autowired
    private lateinit var preferences: Preferences

    @PostConstruct
    fun init() = runBlocking {
        preferences.currencies.forEach {
            /*rateRepository.createOrUpdate(it.symbol, it.symbol, RateSource.MARKET, BigDecimal.ONE)
                .awaitSingleOrNull()
            rateRepository.createOrUpdate(it.symbol, it.symbol, RateSource.EXTERNAL, BigDecimal.ONE)
                .awaitSingleOrNull()*/
        }
    }
}
