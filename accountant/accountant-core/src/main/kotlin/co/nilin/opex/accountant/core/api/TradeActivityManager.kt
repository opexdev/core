package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.DailyAmount

interface TradeActivityManager {
    suspend fun getLastDaysTradeActivity(
        userId: String,
        quoteCurrency: String? = null,
        n: Int = 31
    ): List<DailyAmount>
}