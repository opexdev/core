package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class TransferReserveRequest(
    val sourceAmount: BigDecimal,
    val sourceSymbol: String,
    val destSymbol: String,
    var senderUuid: String?,
    val receiverUuid: String,
)