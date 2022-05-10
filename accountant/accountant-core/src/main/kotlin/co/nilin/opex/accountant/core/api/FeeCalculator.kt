package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.Order
import co.nilin.opex.matching.engine.core.eventh.events.TradeEvent

interface FeeCalculator {

    suspend fun createMakerFeeAction(trade: TradeEvent, makerOrder: Order, takerOrder: Order)

    suspend fun createTakerFeeAction(trade: TradeEvent, makerOrder: Order, takerOrder: Order)

}