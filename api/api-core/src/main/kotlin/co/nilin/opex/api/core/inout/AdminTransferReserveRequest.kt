package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class AdminTransferReserveRequest(
    val sourceSymbol: String,
    val sourceAmount: BigDecimal,
    val destSymbol: String,
    val destAmount: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val receiverUuid: String,
)