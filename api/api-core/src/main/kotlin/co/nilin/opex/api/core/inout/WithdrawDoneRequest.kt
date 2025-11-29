package co.nilin.opex.api.core.inout

import java.math.BigDecimal

data class WithdrawDoneRequest(
    val destTransactionRef: String,
    val destNote: String?,
    val destAmount: BigDecimal?,
    val attachment: String?
)
