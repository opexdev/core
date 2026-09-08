package co.nilin.opex.api.core.inout.pricemanagement

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProviderPrice(
    val provider: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
)
