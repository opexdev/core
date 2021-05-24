package co.nilin.mixchange.accountant.core.api

import co.nilin.mixchange.accountant.core.model.FinancialAction
import co.nilin.mixchange.matching.core.eventh.events.*

interface OrderManager {
    suspend fun handleRequestOrder(submitOrderEvent: SubmitOrderEvent): List<FinancialAction>
    suspend fun handleNewOrder(createOrderEvent: CreateOrderEvent): List<FinancialAction>
    suspend fun handleUpdateOrder(updatedOrderEvent: UpdatedOrderEvent): List<FinancialAction>
    suspend fun handleRejectOrder(rejectOrderEvent: RejectOrderEvent): List<FinancialAction>
    suspend fun handleCancelOrder(cancelOrderEvent: CancelOrderEvent): List<FinancialAction>
}