package co.nilin.opex.price.core.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProviderPrice(
    val provider: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
)

data class SymbolPrices(
    val symbol: String,
    val prices: List<ProviderPrice>
)
