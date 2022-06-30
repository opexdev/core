package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.CurrencyRate

interface MarketRateService {

    suspend fun currencyRate(currency: String, basedOnCurrency: String): CurrencyRate

    suspend fun currencyRate(basedOn: String): List<CurrencyRate>

}