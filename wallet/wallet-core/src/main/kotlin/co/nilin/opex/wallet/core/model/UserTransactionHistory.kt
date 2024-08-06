package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserTransactionHistory(
    val id: String,
    val userId: String,
    val currency: String,
    val balanceChange: BigDecimal,
    val balanceBefore: BigDecimal,
    val category: UserTransactionCategory,
    val description: String?,
    val date: LocalDateTime
)
