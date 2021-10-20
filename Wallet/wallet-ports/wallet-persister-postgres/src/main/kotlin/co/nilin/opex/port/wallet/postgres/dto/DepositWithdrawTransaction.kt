package co.nilin.opex.port.wallet.postgres.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class DepositWithdrawTransaction(
    val id: Long,
    val currency: String,
    val amount: BigDecimal,
    val description: String?,
    val date: LocalDateTime
)