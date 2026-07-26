package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class UserTransactionHistory(
    val id: String,
    val userId: String,
    val ownerName: String?,
    val currency: String,
    val balance: BigDecimal,
    val balanceChange: BigDecimal,
    val category: UserTransactionCategory,
    val description: String?,
    val date: LocalDateTime
)
