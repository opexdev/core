package co.nilin.opex.wallet.core.inout

class WithdrawRejectCommand(
        val withdrawId: String,
        val statusReason: String,
        val destNote: String?,
        val applicator: String?
)