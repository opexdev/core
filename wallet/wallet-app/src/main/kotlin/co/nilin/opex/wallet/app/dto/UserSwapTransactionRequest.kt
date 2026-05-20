package co.nilin.opex.wallet.app.dto

import co.nilin.opex.wallet.core.model.UserTransactionCategory
import co.nilin.opex.wallet.core.model.otc.ReservedStatus

data class UserSwapTransactionRequest(
    val userId: String? = null,
    val currency: String?,
    val sourceSymbol: String?,
    val destSymbol: String?,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 10,
    val offset: Int? = 0,
    val ascendingByTime: Boolean = false,
    val status: ReservedStatus? = ReservedStatus.Committed
)

