package co.nilin.opex.wallet.ports.postgres.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionWithDetail(
    val id: Long,
    val srcWallet: String,
    val destWallet: String,
    val senderUuid: String,
    val receiverUuid: String,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: LocalDateTime,
    val category: String,
    val detail: String?,
)