package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.WithdrawStatus

data class WithdrawHistoryRequest(
    val currency: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false,
    val status: WithdrawStatus? = null
)
