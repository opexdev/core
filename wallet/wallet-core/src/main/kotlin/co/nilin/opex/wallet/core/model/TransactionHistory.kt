package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

data class TransactionHistory(
    val id: Long,
    val currency: String,
    val wallet: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: Long,
    val category: String?
)