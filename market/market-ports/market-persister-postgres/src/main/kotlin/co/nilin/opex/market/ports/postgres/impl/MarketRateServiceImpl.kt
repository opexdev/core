package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.spi.MarketRateService
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MarketRateServiceImpl(private val rateRepository: CurrencyRateRepository) : MarketRateService {

    override suspend fun currencyRate(basedOn: String): List<CurrencyRate> {
        return rateRepository.findAllByDestinationCurrency(basedOn)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { CurrencyRate(it.source, it.destination, it.rate) }
    }

    override suspend fun currencyRate(currency: String, basedOn: String): CurrencyRate {
        val rate = rateRepository.findBySourceAndDestination(currency, basedOn).awaitSingleOrNull()
        return CurrencyRate(
            currency,
            basedOn,
            rate?.rate ?: BigDecimal.ZERO
        )
    }

    override suspend fun indirectRate(basedOn: String): List<CurrencyRate> {
        return rateRepository.findAllByDestinationCurrencyIndirect(basedOn)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { CurrencyRate(it.source, it.destination, it.rate) }
    }

    override suspend fun indirectRate(currency: String, basedOn: String): CurrencyRate {
        val rate = rateRepository.findBySourceAndDestinationIndirect(currency, basedOn).awaitSingleOrNull()
        return CurrencyRate(
            rate?.source ?: currency,
            rate?.destination ?: basedOn,
            rate?.rate ?: BigDecimal.ZERO
        )
    }
}