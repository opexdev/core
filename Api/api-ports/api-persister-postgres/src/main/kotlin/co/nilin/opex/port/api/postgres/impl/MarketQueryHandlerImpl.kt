package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.api.core.inout.MarketTradeResponse
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.core.inout.QueryOrderResponse
import co.nilin.opex.api.core.inout.TradeResponse
import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.port.api.postgres.dao.OrderRepository
import co.nilin.opex.port.api.postgres.dao.TradeRepository
import co.nilin.opex.port.api.postgres.model.OrderModel
import co.nilin.opex.port.api.postgres.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.security.Principal
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

    override suspend fun lastOrder(symbol: String): QueryOrderResponse? {
        return orderRepository.findLastOrderBySymbol(symbol)
            .awaitFirstOrNull()
            ?.asQueryOrderResponse()
    }

    override suspend fun recentTrades(principal: Principal, symbol: String, limit: Int): Flow<MarketTradeResponse> {
        return tradeRepository.findBySymbolSortDescendingByCreateDate(symbol, limit)
            .map {
                val takerOrder = orderRepository.findByOuid(it.takerOuid).awaitFirst()
                val makerOrder = orderRepository.findByOuid(it.makerOuid).awaitFirst()
                val isMakerBuyer = makerOrder.direction == OrderDirection.BID
                MarketTradeResponse(
                    it.symbol,
                    it.tradeId,
                    if (isMakerBuyer) it.makerPrice.toBigDecimal() else it.takerPrice.toBigDecimal(),
                    it.matchedQuantity.toBigDecimal(),
                    if (isMakerBuyer)
                        makerOrder.quoteQuantity!!.toBigDecimal()
                    else
                        takerOrder.quoteQuantity!!.toBigDecimal(),
                    Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                    true,
                    isMakerBuyer
                )
            }
    }

    private fun OrderModel.asQueryOrderResponse() = QueryOrderResponse(
        symbol,
        orderId ?: -1,
        -1,
        clientOrderId ?: "",
        price!!.toBigDecimal(),
        quantity!!.toBigDecimal(),
        executedQuantity!!.toBigDecimal(),
        (accumulativeQuoteQty ?: 0.0).toBigDecimal(),
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