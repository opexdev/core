package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TradeAdminResponse(
    val id: Long,
    val currency: String,
    val sourceOwnerUuid: String,
    val sourceOwnerName: String?,
    val destOwnerUuid: String,
    val destOwnerName: String?,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: LocalDateTime,
)
