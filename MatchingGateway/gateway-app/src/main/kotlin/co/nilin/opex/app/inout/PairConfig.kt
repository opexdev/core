package co.nilin.opex.app.inout

class PairConfig(
    val pair: String,
    val leftSideWalletSymbol: String, //can be same as pair left side
    val rightSideWalletSymbol: String, //can be same as pair right side
    val rightSideFraction: Double,
    val leftSideFraction: Double
)