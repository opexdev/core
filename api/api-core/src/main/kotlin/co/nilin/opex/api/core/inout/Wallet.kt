package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class Wallet(
    val asset: String,
    var balance: BigDecimal,
    var locked: BigDecimal,
    var withdraw: BigDecimal
)