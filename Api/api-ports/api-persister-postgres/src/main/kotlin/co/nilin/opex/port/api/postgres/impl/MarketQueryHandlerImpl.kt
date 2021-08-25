package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.core.inout.QueryOrderResponse
import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.port.api.postgres.dao.OrderRepository
import co.nilin.opex.port.api.postgres.dao.TradeRepository
import co.nilin.opex.port.api.postgres.model.OrderModel
import co.nilin.opex.port.api.postgres.util.*
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.ZoneId
import java.util.*

@Component
class MarketQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
) : MarketQueryHandler {

    override suspend fun openBidOrders(symbol: String, limit: Int): List<QueryOrderResponse> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortDescendingByPrice(
            symbol,
            OrderDirection.BID,
            limit,
            listOf(OrderStatus.NEW.ordinal, OrderStatus.PARTIALLY_FILLED.ordinal)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.asQueryOrderResponse() }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<QueryOrderResponse> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortAscendingByPrice(
            symbol,
            OrderDirection.ASK,
            limit,
            listOf(OrderStatus.NEW.ordinal, OrderStatus.PARTIALLY_FILLED.ordinal)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.asQueryOrderResponse() }
    }

    private fun OrderModel.asQueryOrderResponse() = QueryOrderResponse(
        symbol,
        orderId ?: -1,
        -1,
        clientOrderId ?: "",
        BigDecimal(price!!),
        BigDecimal(quantity!!),
        BigDecimal(executedQuantity!!),
        BigDecimal(accumulativeQuoteQty ?: 0.0),
        status!!.toOrderStatus(),
        constraint!!.toTimeInForce(),
        type!!.toApiOrderType(),
        direction!!.toOrderSide(),
        null,
        null,
        Date.from(createDate!!.atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(updateDate.atZone(ZoneId.systemDefault()).toInstant()),
        status.toOrderStatus().isWorking(),
        quoteQuantity!!.toBigDecimal()
    )
}