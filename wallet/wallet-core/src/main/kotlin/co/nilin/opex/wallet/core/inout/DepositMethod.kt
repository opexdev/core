package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal
import java.util.*

data class DepositMethod(var uuid:String?= UUID.randomUUID().toString(),
                         var type:WithdrawType,
                         var minDeposit: BigDecimal? = BigDecimal.TEN,
                         var maxDeposit: BigDecimal? = BigDecimal.ZERO,)
