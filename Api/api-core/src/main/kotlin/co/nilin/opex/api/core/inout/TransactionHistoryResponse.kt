package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionHistoryResponse(
    val id: Long,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val date: LocalDateTime
)