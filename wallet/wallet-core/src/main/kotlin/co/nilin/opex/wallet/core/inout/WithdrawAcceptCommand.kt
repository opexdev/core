package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

class WithdrawAcceptCommand(
        val withdrawId: String,
        val destTransactionRef: String?,
        val destNote: String?,
        val appliedFee: BigDecimal,
        val applicator: String? = null
)