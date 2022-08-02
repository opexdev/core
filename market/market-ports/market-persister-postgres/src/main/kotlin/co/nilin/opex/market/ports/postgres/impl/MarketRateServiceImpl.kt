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

    override suspend fun currencyRate(baseAsset: String): List<CurrencyRate> {
        return rateRepository.findAllByDestinationCurrency(baseAsset)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { CurrencyRate(it.source, it.destination, it.rate) }
    }

    override suspend fun currencyRate(currency: String, baseAsset: String): CurrencyRate {
        val rate = rateRepository.findBySourceAndDestination(currency, baseAsset).awaitSingleOrNull()
        return CurrencyRate(
            currency,
            baseAsset,
            rate?.rate ?: BigDecimal.ZERO
        )
    }
}