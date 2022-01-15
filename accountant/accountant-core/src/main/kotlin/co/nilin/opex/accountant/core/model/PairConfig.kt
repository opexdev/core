package co.nilin.opex.accountant.core.model

class PairConfig(
    val pair: String,
    val leftSideWalletSymbol: String, //can be same as pair left side
    val rightSideWalletSymbol: String, //can be same as pair right side
    val leftSideFraction: Double,
    val rightSideFraction: Double,
    val rate: Double = Double.NaN
)