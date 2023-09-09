package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class TransferReserveResponse(
    val reserveUuid: String,
    val guaranteedDestAmount: BigDecimal
)