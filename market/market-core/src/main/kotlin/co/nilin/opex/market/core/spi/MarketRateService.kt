package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.CurrencyPrice

interface MarketRateService {

    suspend fun priceOfCurrency(currency: String, basedOnCurrency: String): CurrencyPrice

    suspend fun priceOfAllCurrencies(currency: String): List<CurrencyPrice>

}