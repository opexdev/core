package co.nilin.opex.api.core.inout

import java.math.BigDecimal

class PairConfig(
    val pair: String,
    val leftSideWalletSymbol: String,
    val rightSideWalletSymbol: String,
    val leftSideFraction: BigDecimal,
    val rightSideFraction: BigDecimal
)