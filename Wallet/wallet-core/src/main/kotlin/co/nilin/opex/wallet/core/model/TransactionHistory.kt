package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionHistory(
    val id: Long,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val date: LocalDateTime
)