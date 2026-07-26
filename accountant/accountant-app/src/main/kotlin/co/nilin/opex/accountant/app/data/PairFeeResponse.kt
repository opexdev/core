package co.nilin.opex.accountant.app.data

import java.math.BigDecimal

data class PairFeeResponse(
    val pair: String,
    val direction: String,
    val userLevel: String,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal
)