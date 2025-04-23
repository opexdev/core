package co.nilin.opex.market.core.spi

import co.nilin.opex.market.core.inout.*
import java.util.*

interface UserQueryHandler {

    suspend fun getOrder(uuid: String, ouid: String): Order?

    suspend fun queryOrder(uuid: String, request: QueryOrderRequest): Order?

    suspend fun openOrders(uuid: String, symbol: String?, limit: Int): List<Order>

    suspend fun allOrders(uuid: String, allOrderRequest: AllOrderRequest): List<Order>

    suspend fun allTrades(uuid: String, request: TradeRequest): List<Trade>

    suspend fun txOfTrades(transactionRequest: TransactionRequest): TransactionResponse?

    suspend fun getOrderHistory(
        uuid: String,
        symbol: String?,
        fromDate: Date?,
        toDate: Date?,
        orderType: OrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<OrderData>

}