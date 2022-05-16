package co.nilin.opex.api.core.inout

data class PairFeeResponse(
    val pair:String,
    val direction: String,
    val userLevel: String,
    val makerFee: Double,
    val takerFee: Double
)