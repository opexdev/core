package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

data class TransactionSummary(
    val currency: String,
    val amount: BigDecimal,
)
