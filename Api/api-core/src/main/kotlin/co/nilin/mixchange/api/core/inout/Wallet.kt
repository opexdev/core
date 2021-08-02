package co.nilin.mixchange.api.core.inout

import java.math.BigDecimal

data class Wallet(
    val asset: String,
    val balance: BigDecimal,
    val type: String
)