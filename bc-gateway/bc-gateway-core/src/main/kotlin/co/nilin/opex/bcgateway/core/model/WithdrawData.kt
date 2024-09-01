package co.nilin.opex.bcgateway.core.model

import java.math.BigDecimal

data class WithdrawData(
    val isEnabled: Boolean,
    val fee: BigDecimal,
    val minimum: BigDecimal
)
