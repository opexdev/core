package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.CurrencyRate

interface MarketRateService {

    suspend fun currencyRate(baseAsset: String): List<CurrencyRate>

    suspend fun currencyRate(currency: String, baseAsset: String): CurrencyRate

}