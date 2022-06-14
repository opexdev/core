package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.UserQueryHandler
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import co.nilin.opex.api.ports.postgres.util.*
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.security.Principal
import java.time.ZoneId
import java.util.*

@Component
class UserQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
    private val orderStatusRepository: OrderStatusRepository
) : UserQueryHandler {

    override suspend fun queryOrder(principal: Principal, request: QueryOrderRequest): QueryOrderResponse? {
        val order = (if (request.origClientOrderId != null) {
            orderRepository.findBySymbolAndClientOrderId(request.symbol, request.origClientOrderId!!)
        } else {
            orderRepository.findBySymbolAndOrderId(request.symbol, request.orderId!!)

        }).awaitFirstOrNull() ?: return null

        if (order.uuid != principal.name)
            throw OpexException(OpexError.Forbidden)

        return order.asQueryResponse(orderStatusRepository.findMostRecentByOUID(order.ouid).awaitFirstOrNull())
    }

    override suspend fun openOrders(principal: Principal, symbol: String?): Flow<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndStatus(
            principal.name,
            symbol,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asQueryResponse(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
    }

    override suspend fun allOrders(principal: Principal, allOrderRequest: AllOrderRequest): Flow<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndTimeBetween(
            principal.name,
            allOrderRequest.symbol,
            allOrderRequest.startTime,
            allOrderRequest.endTime
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asQueryResponse(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
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
                    trade.takerPrice
                } else {
                    trade.makerPrice
                },
                trade.matchedQuantity,
                if (isMakerBuyer) {
                    makerOrder.quoteQuantity!!
                } else {
                    takerOrder.quoteQuantity!!
                },
                if (trade.takerUuid == principal.name) {
                    trade.takerCommision!!
                } else {
                    trade.makerCommision!!
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


    private fun OrderModel.asQueryResponse(orderStatusModel: OrderStatusModel?) = QueryOrderResponse(
        symbol,
        ouid,
        orderId ?: -1,
        -1,
        clientOrderId ?: "",
        price!!,
        quantity!!,
        orderStatusModel?.executedQuantity ?: BigDecimal.ZERO,
        orderStatusModel?.accumulativeQuoteQty ?: BigDecimal.ZERO,
        orderStatusModel?.status?.toOrderStatus() ?: OrderStatus.NEW,
        constraint!!.toTimeInForce(),
        type!!.toApiOrderType(),
        direction!!.toOrderSide(),
        null,
        null,
        Date.from(createDate!!.atZone(ZoneId.systemDefault()).toInstant()),
        Date.from(updateDate.atZone(ZoneId.systemDefault()).toInstant()),
        (orderStatusModel?.status?.toOrderStatus() ?: OrderStatus.NEW).isWorking(),
        quoteQuantity!!
    )
}