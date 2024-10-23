package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.TransferCategory

data class TransactionRequest(
    val coin: String?,
    val category: TransferCategory?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false
)