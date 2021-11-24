package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class TransactionHistoryResponse(
    val id: Long,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: Long
)