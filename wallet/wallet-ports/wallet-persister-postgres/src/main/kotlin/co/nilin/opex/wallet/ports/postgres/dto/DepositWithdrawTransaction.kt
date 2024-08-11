package co.nilin.opex.wallet.ports.postgres.dto

import co.nilin.opex.wallet.core.model.TransferCategory
import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositWithdrawTransaction(
    val id: Long,
    val wallet: String,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val ref: String?,
    val date: LocalDateTime,
    val category: TransferCategory
)