package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

class PairFeeConfig(
    val pairConfig: PairConfig,
    val direction: String,
    val userLevel: String,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal
)