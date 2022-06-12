package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.model.OrderStatusModel
import co.nilin.opex.market.ports.postgres.util.*
import co.nilin.opex.market.core.inout.AllOrderRequest
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.core.inout.QueryOrderRequest
import co.nilin.opex.market.core.inout.QueryOrderResponse
import co.nilin.opex.market.core.spi.UserQueryHandler
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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

    override suspend fun queryOrder(uuid: String, request: QueryOrderRequest): QueryOrderResponse? {
        val order = (if (request.origClientOrderId != null) {
            orderRepository.findBySymbolAndClientOrderId(request.symbol, request.origClientOrderId!!)
        } else {
            orderRepository.findBySymbolAndOrderId(request.symbol, request.orderId!!)

        }).awaitFirstOrNull() ?: return null

        if (order.uuid != uuid)
            throw OpexException(OpexError.Forbidden)

        return order.asQueryResponse(orderStatusRepository.findMostRecentByOUID(order.ouid).awaitFirstOrNull())
    }

    override suspend fun openOrders(uuid: String, symbol: String?): List<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndStatus(
            uuid,
            symbol,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asQueryResponse(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
            .toList()
    }

    override suspend fun allOrders(uuid: String, allOrderRequest: AllOrderRequest): List<QueryOrderResponse> {
        return orderRepository.findByUuidAndSymbolAndTimeBetween(
            uuid,
            allOrderRequest.symbol,
            allOrderRequest.startTime,
            allOrderRequest.endTime
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asQueryResponse(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
            .toList()
    }

    override suspend fun allTrades(uuid: String, request: TradeRequest): List<TradeResponse> {
        return tradeRepository.findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
            uuid, request.symbol, request.fromTrade, request.startTime, request.endTime
        ).map { trade ->
            val takerOrder = orderRepository.findByOuid(trade.takerOuid).awaitFirst()
            val makerOrder = orderRepository.findByOuid(trade.makerOuid).awaitFirst()
            val isMakerBuyer = makerOrder.direction == OrderDirection.BID
            TradeResponse(
                trade.symbol,
                trade.tradeId,
                if (trade.takerUuid == uuid) {
                    takerOrder.orderId!!
                } else {
                    makerOrder.orderId!!
                },
                -1,
                if (trade.takerUuid == uuid) {
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
                if (trade.takerUuid == uuid) {
                    trade.takerCommision!!
                } else {
                    trade.makerCommision!!
                },
                if (trade.takerUuid == uuid) {
                    trade.takerCommisionAsset!!
                } else {
                    trade.makerCommisionAsset!!
                },
                Date.from(
                    trade.createDate.atZone(ZoneId.systemDefault()).toInstant()
                ),
                if (trade.takerUuid == uuid) {
                    OrderDirection.ASK == takerOrder.direction
                } else {
                    OrderDirection.ASK == makerOrder.direction
                },
                trade.makerUuid == uuid,
                true,
                isMakerBuyer
            )
        }.toList()
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