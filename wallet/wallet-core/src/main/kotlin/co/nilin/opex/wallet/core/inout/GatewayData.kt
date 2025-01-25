package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

data class GatewayData(
    val isEnabled: Boolean,
    val fee: BigDecimal,
    val minimum: BigDecimal,
    val maximum: BigDecimal? = null
)