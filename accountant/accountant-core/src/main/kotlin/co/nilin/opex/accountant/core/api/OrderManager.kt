package co.nilin.opex.accountant.core.api

import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.matching.engine.core.eventh.events.*

interface OrderManager {
    suspend fun handleRequestOrder(submitOrderEvent: SubmitOrderEvent): List<FinancialAction>
    suspend fun handleNewOrder(createOrderEvent: CreateOrderEvent): List<FinancialAction>
    suspend fun handleUpdateOrder(updatedOrderEvent: UpdatedOrderEvent): List<FinancialAction>
    suspend fun handleRejectOrder(rejectOrderEvent: RejectOrderEvent): List<FinancialAction>
    suspend fun handleCancelOrder(cancelOrderEvent: CancelOrderEvent): List<FinancialAction>
}