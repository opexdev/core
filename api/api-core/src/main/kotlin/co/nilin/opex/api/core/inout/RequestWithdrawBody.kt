package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class RequestWithdrawBody(
    val currency: String,
    val amount: BigDecimal,
    val destSymbol: String?,
    val destAddress: String,
    val destNetwork: String?,
    val destNote: String?,
    val gatewayUuid: String?
)
