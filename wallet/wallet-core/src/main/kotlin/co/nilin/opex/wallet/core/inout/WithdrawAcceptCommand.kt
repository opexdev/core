package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

class WithdrawAcceptCommand(
    val withdrawId: Long,
    val destAmount: BigDecimal?,
    val destTransactionRef: String,
    val destNote: String?
)