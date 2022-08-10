package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.core.spi.MarketRateService
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MarketRateServiceImpl(private val rateRepository: CurrencyRateRepository) : MarketRateService {

    override suspend fun currencyRate(quote: String, source: RateSource): List<CurrencyRate> {
        return rateRepository.findAllByQuoteAndSource(quote, source)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { CurrencyRate(it.base, it.quote, it.source, it.rate) }
    }

    override suspend fun currencyRate(base: String, quote: String, source: RateSource): CurrencyRate {
        val rate = rateRepository.findByBaseAndQuoteAndSource(base, quote, source).awaitSingleOrNull()
        return CurrencyRate(
            base,
            quote,
            source,
            rate?.rate ?: BigDecimal.ZERO
        )
    }
}