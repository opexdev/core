package co.nilin.opex.wallet.core.inout

import java.math.BigDecimal

class WithdrawDoneCommand(
    val withdrawId: String,
    val destAmount: BigDecimal?,
    val destTransactionRef: String,
    val destNote: String?,
    var attachment: String?,
    var applicator: String,
)