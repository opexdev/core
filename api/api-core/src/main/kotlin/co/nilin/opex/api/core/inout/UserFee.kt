package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class UserFee(
    val name: String,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal
    )


