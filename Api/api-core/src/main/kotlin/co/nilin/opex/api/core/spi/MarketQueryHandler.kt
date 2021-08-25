package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.QueryOrderResponse

interface MarketQueryHandler {

    suspend fun openBidOrders(symbol: String, limit: Int): List<QueryOrderResponse>

    suspend fun openAskOrders(symbol: String, limit: Int): List<QueryOrderResponse>

}