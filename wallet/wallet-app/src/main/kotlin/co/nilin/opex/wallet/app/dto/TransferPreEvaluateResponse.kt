package co.nilin.opex.wallet.app.dto

import java.math.BigDecimal

data class TransferPreEvaluateResponse(
    val destAmount: BigDecimal
)