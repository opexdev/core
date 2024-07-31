package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Transaction(
    val sourceWallet: Wallet,
    val destWallet: Wallet,
    val sourceAmount: BigDecimal,
    val destAmount: BigDecimal,
    val description: String?,
    val transferRef: String?,
    val transferCategory: TransferCategory,
    val transactionDate: LocalDateTime
)