package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.CurrencyPrice
import co.nilin.opex.market.core.spi.MarketRateService
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.flow.*
import java.math.BigDecimal

class MarketRateServiceImpl(private val tradeRepository: TradeRepository) : MarketRateService {

    override suspend fun priceOfCurrency(currency: String, basedOnCurrency: String): CurrencyPrice {
        val lastTrade = tradeRepository.findMostRecentBySymbol("${currency}_${basedOnCurrency}").singleOrNull()
        return CurrencyPrice(
            currency,
            basedOnCurrency,
            with(lastTrade) { this?.takerPrice?.min(makerPrice) ?: BigDecimal.ZERO }
        )
    }

    override suspend fun priceOfAllCurrencies(currency: String): List<CurrencyPrice> {
        TODO("Not yet implemented")
    }
}