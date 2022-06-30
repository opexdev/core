package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.spi.MarketRateService
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MarketRateServiceImpl(private val rateRepository: CurrencyRateRepository) : MarketRateService {

    override suspend fun currencyRate(currency: String, basedOnCurrency: String): CurrencyRate {
        val rate = rateRepository.findBySourceAndDestination(currency, basedOnCurrency).awaitSingleOrNull()
            ?: rateRepository.findBySourceAndDestinationIndirect(currency, basedOnCurrency).awaitFirstOrNull()
        return CurrencyRate(
            currency,
            basedOnCurrency,
            rate?.rate ?: BigDecimal.ZERO
        )
    }

    override suspend fun currencyRate(basedOn: String): List<CurrencyRate> {
        return rateRepository.findAllByDestinationCurrency(basedOn)
            .collectList()
            .switchIfEmpty(rateRepository.findAllByDestinationCurrencyIndirect(basedOn).collectList())
            .awaitFirstOrElse { emptyList() }
            .map { CurrencyRate(it.sourceCurrency, it.destinationCurrency, it.rate) }
    }
}