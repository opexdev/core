package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class TransferReserveRequest(
    val sourceAmount: BigDecimal,
    val sourceSymbol: String,
    val destSymbol: String,
    var senderUuid: String?,
    val receiverUuid: String,
)