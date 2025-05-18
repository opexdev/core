package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class TransactionSummary(
    val currency: String,
    val amount: BigDecimal,
)
