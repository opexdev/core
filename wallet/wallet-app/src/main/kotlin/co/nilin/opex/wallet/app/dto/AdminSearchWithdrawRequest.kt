package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.WithdrawStatus

data class AdminSearchWithdrawRequest(
    val uuid: String?,
    val currency: String?,
    val destTxRef: String?,
    val destAddress: String?,
    val status: List<WithdrawStatus> = emptyList(),
)
