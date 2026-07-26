package co.nilin.opex.wallet.core.inout

class WithdrawRejectCommand(
    val withdrawId: String,
    val statusReason: String,
    var attachmemt: String?,
    var applicator: String
)