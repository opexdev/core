package co.nilin.opex.wallet.core.inout

class WithdrawRejectCommand(
    val withdrawId: Long,
    val statusReason: String,
)