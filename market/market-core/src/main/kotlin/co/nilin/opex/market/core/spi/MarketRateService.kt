package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.CurrencyRate
import co.nilin.opex.market.core.inout.RateSource

interface MarketRateService {

    suspend fun currencyRate(quote: String, source: RateSource): List<CurrencyRate>

    suspend fun currencyRate(base: String, quote: String, source: RateSource): CurrencyRate

}