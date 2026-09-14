package co.nilin.opex.price.ports.proxy.utils

import co.nilin.opex.price.core.dto.ProviderPrice
import co.nilin.opex.price.core.dto.SymbolPrices
import co.nilin.opex.price.ports.proxy.dto.ProviderPriceEntry
import co.nilin.opex.price.ports.proxy.dto.SymbolPriceResponse

fun ProviderPriceEntry.asCoreModel() = ProviderPrice(
    provider = provider,
    price = price,
    timestamp = timestamp
)

fun SymbolPriceResponse.asCoreModel() = SymbolPrices(
    symbol = symbol,
    prices = prices.map { it.asCoreModel() }
)
