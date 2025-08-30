package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class UserFee(
    val name: String,
    val displayOrder: Int,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,

    )


