package co.nilin.opex.price.ports.proxy.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProviderPriceEntry(
    val provider: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
)

data class SymbolPriceResponse(
    val symbol: String,
    val prices: List<ProviderPriceEntry>
)

data class AllLatestPriceResponse(
    val items: List<SymbolPriceResponse>
)
