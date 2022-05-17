package co.nilin.opex.accountant.app.data

data class PairFeeResponse(
    val pair:String,
    val direction: String,
    val userLevel: String,
    val makerFee: Double,
    val takerFee: Double
)