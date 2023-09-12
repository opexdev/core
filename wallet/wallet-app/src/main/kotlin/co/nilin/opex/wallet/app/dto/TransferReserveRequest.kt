package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class TransferReserveRequest(
    val amount: BigDecimal,
    val sourceSymbol: String,
    val destSymbol: String,
    val senderUuid: String,
    val senderWalletType: String,
    val receiverUuid: String,
    val receiverWalletType: String
)