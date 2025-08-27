package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.FeeConfig
import co.nilin.opex.accountant.core.model.FeeFinancialActions
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent

interface FeeCalculator {

    suspend fun getUserFee(uuid: String): FeeConfig

    suspend fun createFeeActions(
        trade: TradeEvent,
        makerOrder: Order,
        takerOrder: Order,
        makerParentFA: FinancialAction?,
        takerParentFA: FinancialAction?
    ): FeeFinancialActions

}