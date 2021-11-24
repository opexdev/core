package co.nilin.opex.websocket.ports.postgres.impl

import co.nilin.opex.websocket.core.inout.*
import co.nilin.opex.websocket.core.spi.UserQueryHandler
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.websocket.ports.postgres.dao.OrderRepository
import co.nilin.opex.websocket.ports.postgres.dao.TradeRepository
import co.nilin.opex.websocket.ports.postgres.model.OrderModel
import co.nilin.opex.port.websocket.postgres.util.*
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.websocket.ports.postgres.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.security.Principal
import java.time.ZoneId
import java.util.*

@Component
class UserQueryHandlerImpl(
    val orderRepository: OrderRepository,
    val tradeRepository: TradeRepository
) : UserQueryHandler {

    override suspend fun queryOrder(principal: Principal, request: QueryOrderRequest): QueryOrderResponse? {
        val order = (if (request.origClientOrderId != null) {
            orderRepository.findBySymbolAndClientOrderId(request.symbol, request.origClientOrderId!!)
        } else {
            orderRepository.findBySymbolAndOrderId(request.symbol, request.orderId!!)

        }).awaitFirstOrNull()
        if (order?.constraint != null) {
            if (order.uuid != principal.name)
                throw OpexException(OpexError.Forbidden)
            return orderToQueryResponse(order)
        }
        return null
    }

    override suspend fun openOrders(principal: Principal, symbol: String?): Flow<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndStatus(
            principal.name,
            symbol,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).filter { orderModel -> orderModel.constraint != null }
            .map { order -> orderToQueryResponse(order) }
    }

    override suspend fun allOrders(principal: Principal, allOrderRequest: AllOrderRequest): Flow<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndTimeBetween(
            principal.name,
            allOrderRequest.symbol,
            allOrderRequest.startTime,
            allOrderRequest.endTime
        ).filter { orderModel -> orderModel.constraint != null }
            .map { order -> orderToQueryResponse(order) }
    }

    override suspend fun allTrades(principal: Principal, request: TradeRequest): Flow<TradeResponse> {
        return tradeRepository.findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
            principal.name, request.symbol, request.fromTrade, request.startTime, request.endTime
        ).map { trade ->
            val takerOrder = orderRepository.findByOuid(trade.takerOuid).awaitFirst()
            val makerOrder = orderRepository.findByOuid(trade.makerOuid).awaitFirst()
            val isMakerBuyer = makerOrder.direction == OrderDirection.BID
            TradeResponse(
                trade.symbol,
                trade.tradeId,
                if (trade.takerUuid == principal.name) {
                    takerOrder.orderId!!
                } else {
                    makerOrder.orderId!!
                },
                -1,
                if (trade.takerUuid == principal.name) {
                    trade.takerPrice.toBigDecimal()
                } else {
                    trade.makerPrice.toBigDecimal()
                },
                trade.matchedQuantity.toBigDecimal(),
                if (isMakerBuyer) {
                    makerOrder.quoteQuantity!!.toBigDecimal()
                } else {
                    takerOrder.quoteQuantity!!.toBigDecimal()
                },
                if (trade.takerUuid == principal.name) {
                    trade.takerCommision!!.toBigDecimal()
                } else {
                    trade.makerCommision!!.toBigDecimal()
                },
                if (trade.takerUuid == principal.name) {
                    trade.takerCommisionAsset!!
                } else {
                    trade.makerCommisionAsset!!
                },
                Date.from(
                    trade.createDate.atZone(ZoneId.systemDefault()).toInstant()
                ),
                if (trade.takerUuid == principal.name) {
                    OrderDirection.ASK == takerOrder.direction
                } else {
                    OrderDirection.ASK == makerOrder.direction
                },
                trade.makerUuid == principal.name,
                true,
                isMakerBuyer
            )
        }
    }


    private fun orderToQueryResponse(order: OrderModel) = QueryOrderResponse(
        order.symbol,
        order.ouid,
        order.orderId ?: -1,
        -1,
        order.clientOrderId ?: "",
        order.price!!.toBigDecimal(),
        order.quantity!!.toBigDecimal(),
        order.executedQuantity!!.toBigDecimal(),
        (order.accumulativeQuoteQty ?: 0.0).toBigDecimal(),
        order.status!!.toOrderStatus(),
        order.constraint!!.toTimeInForce(),
        order.type!!.toWebSocketOrderType(),
        order.direction!!.toOrderSide(),
        null,
        null,
        Date.from(order.createDate!!.atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(order.updateDate.atZone(ZoneId.systemDefault()).toInstant()),
        order.status.toOrderStatus().isWorking(), order.quoteQuantity!!.toBigDecimal()
    )
}