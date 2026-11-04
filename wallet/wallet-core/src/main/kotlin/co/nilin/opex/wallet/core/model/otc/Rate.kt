package co.nilin.opex.wallet.core.model.otc

import java.math.BigDecimal
import java.time.LocalDateTime

data class Rate(
    val sourceSymbol: String, val destSymbol: String, val rate: BigDecimal
)

data class StaleRate(
    val sourceSymbol: String,
    val destSymbol: String,
    val lastUpdateDate: LocalDateTime
)

data class Rates(
    var rates: List<Rate>?
)