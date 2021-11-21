package co.nilin.opex.api.core.inout

data class PairInfoResponse(
    val pair: String,
    val leftSideWalletSymbol: String,
    val rightSideWalletSymbol: String,
    val leftSideFraction: Double,
    val rightSideFraction: Double
)