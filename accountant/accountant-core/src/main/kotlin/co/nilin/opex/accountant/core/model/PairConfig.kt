package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

class PairConfig(
    val pair: String,
    val leftSideWalletSymbol: String, //can be same as pair left side
    val rightSideWalletSymbol: String, //can be same as pair right side
    val leftSideFraction: BigDecimal,
    val rightSideFraction: BigDecimal
)