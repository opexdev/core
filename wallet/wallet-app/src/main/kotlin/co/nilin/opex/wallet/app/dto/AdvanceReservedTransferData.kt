package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal
import java.util.*

data class AdvanceReservedTransferData(
    val sourceSymbol: String,
    val destSymbol: String,
    val senderWalletType: String,
    val senderUuid: String,
    val receiverWalletType: String,
    val receiverUuid: String,
    val sourceAmount: BigDecimal,
    val reservedDestAmount: BigDecimal,
    val reserveTime: Date
)