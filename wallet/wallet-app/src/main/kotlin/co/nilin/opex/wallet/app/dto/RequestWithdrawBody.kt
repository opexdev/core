package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class RequestWithdrawBody(
    val currency: String,
    val amount: BigDecimal,
    val destSymbol: String?,
    val destAddress: String,
    val destNetwork: String?,
    val destNote: String?,
    val description: String?,
    val gatewayUuid:String?
)
