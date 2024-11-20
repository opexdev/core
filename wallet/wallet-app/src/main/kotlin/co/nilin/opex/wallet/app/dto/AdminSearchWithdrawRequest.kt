package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.DepositStatus
import co.nilin.opex.wallet.core.model.WithdrawStatus

data class AdminSearchWithdrawRequest(
    val uuid: String?,
    val currency: String?,
    val destTxRef: String?,
    val destAddress: String?,
    val status: List<WithdrawStatus> = emptyList(),
    val startTime: Long? = null,
    val endTime: Long? = null,
    val ascendingByTime: Boolean = false
)

data class AdminSearchDepositRequest(
    val uuid: String?,
    val currency: String?,
    val sourceAddress: String?,
    val transactionRef: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val status: List<DepositStatus>? = null,
    val ascendingByTime: Boolean = false
)
