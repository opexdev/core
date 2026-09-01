package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.ProviderPrice
import co.nilin.opex.price.core.dto.SymbolPrices

interface PriceProxy {
    suspend fun getPrices(symbols: List<String>): List<SymbolPrices>
    suspend fun getProvidersPrice(symbol: String): List<ProviderPrice>
}
