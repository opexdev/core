package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class UserCurrencyVolume(
    val currency: String,
    val volume: BigDecimal,
    val valueUSDT: BigDecimal,
    val valueIRT: BigDecimal
)