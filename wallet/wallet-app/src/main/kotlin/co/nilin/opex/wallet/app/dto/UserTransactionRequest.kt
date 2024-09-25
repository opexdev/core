package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.UserTransactionCategory

data class UserTransactionRequest(
    val currency: String?,
    val category: UserTransactionCategory?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false
)