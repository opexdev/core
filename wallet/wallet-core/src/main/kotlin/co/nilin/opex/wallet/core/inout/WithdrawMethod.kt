package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.util.UUID

data class WithdrawMethod (
        var uuid:String?=UUID.randomUUID().toString(),
        var type:WithdrawType,
        var minWithdraw: BigDecimal? = BigDecimal.TEN,
        var maxWithdraw: BigDecimal? = BigDecimal.ZERO,
        )