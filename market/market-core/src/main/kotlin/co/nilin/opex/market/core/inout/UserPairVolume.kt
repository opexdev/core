package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class UserPairVolume(
    val pair: String,
    val volume: BigDecimal
)