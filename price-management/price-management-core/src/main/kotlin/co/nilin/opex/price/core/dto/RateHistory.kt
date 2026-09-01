package co.nilin.opex.price.core.dto

import java.time.LocalDateTime

data class RateHistory(
    val symbol: String,
    val price: java.math.BigDecimal,
    val createdDate: LocalDateTime,
    val source: PriceMode
)
