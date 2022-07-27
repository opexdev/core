package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.CurrencyRate

interface MarketRateService {

    suspend fun currencyRate(basedOn: String): List<CurrencyRate>

    suspend fun currencyRate(currency: String, basedOn: String): CurrencyRate

    suspend fun indirectRate(basedOn: String): List<CurrencyRate>

    suspend fun indirectRate(currency: String, basedOn: String): CurrencyRate

}