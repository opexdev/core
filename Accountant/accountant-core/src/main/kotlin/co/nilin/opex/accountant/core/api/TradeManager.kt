package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.matching.core.eventh.events.TradeEvent

interface TradeManager {
    suspend fun handleTrade(trade: TradeEvent): List<FinancialAction>
}