package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class PairInfoResponse(
    val pair: String,
    val leftSideWalletSymbol: String,
    val rightSideWalletSymbol: String,
    val leftSideFraction: BigDecimal,
    val rightSideFraction: BigDecimal
)