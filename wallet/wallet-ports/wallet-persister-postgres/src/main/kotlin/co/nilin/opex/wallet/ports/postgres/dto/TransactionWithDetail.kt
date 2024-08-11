package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.model.TransferCategory
import co.nilin.opex.wallet.core.model.WalletType
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionWithDetail(
    val id: Long,
    val srcWalletType: WalletType,
    val destWalletType: WalletType,
    val senderUuid: String,
    val receiverUuid: String,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: LocalDateTime,
    val category: TransferCategory,
)