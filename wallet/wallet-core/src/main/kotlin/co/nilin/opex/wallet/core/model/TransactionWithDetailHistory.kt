package co.nilin.opex.wallet.core.model

import java.math.BigDecimal

data class TransactionWithDetailHistory(
    val id: Long,
    val srcWallet: String,
    val destWallet: String,
    val senderUuid: String,
    val receiverUuid: String,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: Long,
    val category: String,
    val withdraw: Boolean? = null
)