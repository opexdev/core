package co.nilin.opex.matching.gateway.app.inout

import java.math.BigDecimal

data class PairFeeConfig(
    val pairConfig: PairConfig,
    val direction: String?,
    val userLevel: String?,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal
)
