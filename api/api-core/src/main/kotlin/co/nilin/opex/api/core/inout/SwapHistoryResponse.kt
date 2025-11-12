package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class SwapHistoryResponse(
    val uuid: String,
    val ownerName: String?,
    val sourceSymbol: String,
    val destSymbol: String,
    val sourceAmount: BigDecimal,
    val reservedDestAmount: BigDecimal,
    val rate: BigDecimal,
)
