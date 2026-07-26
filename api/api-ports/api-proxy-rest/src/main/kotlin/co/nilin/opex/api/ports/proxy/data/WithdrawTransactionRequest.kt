package co.nilin.opex.api.ports.proxy.data

import co.nilin.opex.api.core.inout.WithdrawStatus

data class WithdrawTransactionRequest(
    val currency: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int?,
    val offset: Int?,
    val ascendingByTime: Boolean? = false,
    val status: WithdrawStatus? = null,
)