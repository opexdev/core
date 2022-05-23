package co.nilin.opex.matching.gateway.app.inout

data class PairFeeConfig(
    val pairConfig: PairConfig,
    val direction: String?,
    val userLevel: String?,
    val makerFee: Double,
    val takerFee: Double
)
