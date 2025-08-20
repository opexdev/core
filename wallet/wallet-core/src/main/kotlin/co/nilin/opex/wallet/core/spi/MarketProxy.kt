package co.nilin.opex.wallet.core.spi

import co.nilin.opex.wallet.core.inout.PriceTicker

interface MarketProxy {

    suspend fun fetchPrices(symbol : String? = null): List<PriceTicker>
}