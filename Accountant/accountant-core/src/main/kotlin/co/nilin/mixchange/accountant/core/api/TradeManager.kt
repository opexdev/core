package co.nilin.mixchange.accountant.core.api

import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.matching.core.eventh.events.TradeEvent

interface TradeManager {
    suspend fun handleTrade(trade:TradeEvent): List<FinancialAction>
}