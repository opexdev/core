package co.nilin.opex.websocket.app.service

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichOrderUpdate
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.websocket.app.config.AppDispatchers
import co.nilin.opex.websocket.app.dto.OrderResponse
import co.nilin.opex.websocket.app.utils.*
import co.nilin.opex.websocket.core.inout.TradeResponse
import co.nilin.opex.websocket.core.spi.EventStreamHandler
import co.nilin.opex.websocket.ports.postgres.dao.OrderRepository
import co.nilin.opex.websocket.ports.postgres.model.OrderModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class EventStreamHandlerImpl(
    private val template: SimpMessagingTemplate,
    private val orderRepository: OrderRepository,
    private val registry: SimpUserRegistry
) : EventStreamHandler {

    override fun handleOrder(order: RichOrder) {
        val response = OrderResponse(
            order.ouid,
            order.pair,
            order.orderId ?: -1,
            -1,
            null,
            order.price,
            order.quantity,
            order.executedQuantity,
            order.accumulativeQuoteQty,
            order.status.toOrderStatus(),
            order.constraint.toTimeInForce(),
            order.type.toWebsocketOrderType(),
            order.direction.toOrderSide(),
            Date(),
            Date(),
            order.status.toOrderStatus().isWorking(),
            order.quoteQuantity
        )
        run { template.convertAndSendToUser(order.uuid, EventDestinations.Order.path, response) }
    }

    override fun handleOrderUpdate(orderUpdate: RichOrderUpdate) {
        run {
            val status = orderUpdate.status.code.toOrderStatus()
            val order = orderRepository.findByOuid(orderUpdate.ouid).awaitFirstOrNull() ?: return@run
            val response = OrderResponse(
                order.ouid,
                order.symbol,
                order.orderId ?: -1,
                -1,
                null,
                order.price?.toBigDecimal() ?: BigDecimal.ZERO,
                orderUpdate.quantity,
                orderUpdate.executedQuantity(),
                orderUpdate.accumulativeQuoteQuantity(),
                status,
                order.constraint?.toTimeInForce(),
                order.type?.toWebsocketOrderType(),
                order.direction?.toOrderSide(),
                Date(),
                Date(),
                status.isWorking(),
                order.quoteQuantity?.toBigDecimal() ?: BigDecimal.ZERO
            )
            template.convertAndSendToUser(order.uuid, EventDestinations.Order.path, response)
        }
    }

    override fun handleTrade(trade: RichTrade) {
        run {
            val takerOrder = orderRepository.findByOuid(trade.takerOuid).awaitFirstOrNull()
            val makerOrder = orderRepository.findByOuid(trade.makerOuid).awaitFirstOrNull()
            if (makerOrder == null || takerOrder == null)
                return@run

            val maker = trade.buildTradeResponse(trade.makerUuid, makerOrder, takerOrder)
            val taker = trade.buildTradeResponse(trade.takerUuid, makerOrder, takerOrder)
            template.convertAndSendToUser(trade.makerUuid, EventDestinations.Trade.path, maker)
            template.convertAndSendToUser(trade.takerUuid, EventDestinations.Trade.path, taker)
        }
    }

    private fun RichTrade.buildTradeResponse(
        uuid: String,
        makerOrder: OrderModel,
        takerOrder: OrderModel
    ): TradeResponse {
        val isMakerBuyer = makerOrder.direction == OrderDirection.BID
        return TradeResponse(
            pair,
            id,
            if (takerUuid == uuid) takerOrder.orderId!! else makerOrder.orderId!!,
            -1,
            if (takerUuid == uuid) takerPrice else makerPrice,
            matchedQuantity,
            if (isMakerBuyer)
                makerOrder.quoteQuantity?.toBigDecimal() ?: BigDecimal.ZERO
            else
                takerOrder.quoteQuantity?.toBigDecimal() ?: BigDecimal.ZERO,
            if (takerUuid == uuid) takerCommision else makerCommision,
            if (takerUuid == uuid) takerCommisionAsset else makerCommisionAsset,
            Date(),
            if (takerUuid == uuid)
                OrderDirection.ASK == takerOrder.direction
            else
                OrderDirection.ASK == makerOrder.direction,
            makerUuid == uuid,
            true,
            isMakerBuyer
        )
    }

    private fun run(action: suspend () -> Unit) {
        runBlocking(AppDispatchers.websocketExecutor) {
            try {
                action()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    enum class EventDestinations(val path: String) {
        Order("/secured/queue/orders"),
        Trade("/secured/queue/trades");

        companion object {
            fun findByPath(path: String): EventDestinations? {
                return values().find { it.path == path }
            }
        }
    }

}