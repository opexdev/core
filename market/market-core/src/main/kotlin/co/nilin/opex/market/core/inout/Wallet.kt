package co.nilin.opex.market.core.inout

import java.math.BigDecimal

data class Wallet(
    val asset: String,
    val balance: BigDecimal,
    val type: String
)