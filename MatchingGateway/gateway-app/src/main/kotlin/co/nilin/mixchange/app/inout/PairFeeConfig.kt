package co.nilin.mixchange.app.inout

class PairFeeConfig(
    val pairConfig: PairConfig,
    val direction: String?,
    val userLevel: String?,
    val makerFee: Double,
    val takerFee: Double
)