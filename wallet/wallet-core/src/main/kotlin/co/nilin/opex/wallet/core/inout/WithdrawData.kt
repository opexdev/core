package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

data class WithdrawData(
    val isEnabled: Boolean,
    val fee: BigDecimal,
    val minimum: BigDecimal
)