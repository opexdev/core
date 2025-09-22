package co.nilin.opex.accountant.core.model

import java.math.BigDecimal

data class UserFee(
    val name: String,
    val displayOrder: Int,
    val makerFee: BigDecimal,
    val takerFee: BigDecimal,
)
