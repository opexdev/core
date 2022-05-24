package co.nilin.opex.matching.gateway.app.inout

import java.math.BigDecimal

class PairConfig(
    val pair: String,
    val leftSideWalletSymbol: String, //can be same as pair left side
    val rightSideWalletSymbol: String, //can be same as pair right side
    val rightSideFraction: BigDecimal,
    val leftSideFraction: BigDecimal
)