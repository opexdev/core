package co.nilin.opex.accountant.ports.postgres.impl

import co.nilin.opex.accountant.core.spi.CurrencyRatePersister
import co.nilin.opex.accountant.ports.postgres.dao.CurrencyRateRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class CurrencyRatePersisterImpl(private val repository: CurrencyRateRepository) : CurrencyRatePersister {

    override suspend fun updateRate(base: String, quote: String, rate: BigDecimal) {
        repository.createOrUpdate(base, quote, rate).awaitSingleOrNull()
    }
}