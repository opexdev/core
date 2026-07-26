package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.DailyAmount

interface WithdrawActivityManager {
    suspend fun getLastDaysWithdrawActivity(
        userId: String,
        quoteCurrency: String? = null,
        n: Int = 31
    ): List<DailyAmount>
}