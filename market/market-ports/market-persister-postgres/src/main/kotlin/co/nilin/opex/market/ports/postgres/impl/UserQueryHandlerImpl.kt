package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.common.OpexError
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.UserQueryHandler
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.util.asOrderDTO
import co.nilin.opex.market.ports.postgres.util.toDto
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class UserQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
    private val orderStatusRepository: OrderStatusRepository,
) : UserQueryHandler {

    //TODO merge order and status fetching in query

    override suspend fun getOrder(uuid: String, ouid: String): Order? {
        return orderRepository.findByUUIDAndOUID(uuid, ouid)
            .awaitSingleOrNull()
            ?.asOrderDTO(orderStatusRepository.findMostRecentByOUID(ouid).awaitFirstOrNull())
    }

    override suspend fun queryOrder(uuid: String, request: QueryOrderRequest): Order? {
        val order = (if (request.origClientOrderId != null) {
            orderRepository.findBySymbolAndClientOrderId(request.symbol, request.origClientOrderId!!)
        } else {
            orderRepository.findBySymbolAndOrderId(request.symbol, request.orderId!!)
        }).awaitFirstOrNull() ?: return null

        if (order.uuid != uuid)
            throw OpexError.Forbidden.exception()

        val status = orderStatusRepository.findMostRecentByOUID(order.ouid).awaitFirstOrNull()
        return order.asOrderDTO(status)
    }

    override suspend fun openOrders(uuid: String, limit: Int): List<Order> {
        return orderRepository.findByUuidAndSymbolAndStatus(
            uuid,
            null,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code),
            limit
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asOrderDTO(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
            .toList()
    }

    override suspend fun openOrders(uuid: String, symbol: String?, limit: Int): List<Order> {
        return orderRepository.findByUuidAndSymbolAndStatus(
            uuid,
            symbol,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code),
            limit
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asOrderDTO(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
            .toList()
    }

    override suspend fun allOrders(uuid: String, allOrderRequest: AllOrderRequest): List<Order> {
        return orderRepository.findByUuidAndSymbolAndTimeBetween(
            uuid,
            allOrderRequest.symbol,
            allOrderRequest.startTime,
            allOrderRequest.endTime,
            allOrderRequest.limit
        ).filter { orderModel -> orderModel.constraint != null }
            .map { it.asOrderDTO(orderStatusRepository.findMostRecentByOUID(it.ouid).awaitFirstOrNull()) }
            .toList()
    }

    override suspend fun allTrades(uuid: String, request: TradeRequest): List<Trade> {
        return tradeRepository.findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
            uuid, request.symbol, request.fromTrade, request.startTime, request.endTime, request.limit
        ).map {
            val takerOrder = orderRepository.findByOuid(it.takerOuid).awaitFirst()
            val makerOrder = orderRepository.findByOuid(it.makerOuid).awaitFirst()
            val isMakerBuyer = makerOrder.direction == OrderDirection.BID
            Trade(
                it.symbol,
                it.tradeId,
                if (it.takerUuid == uuid) takerOrder.orderId!! else makerOrder.orderId!!,
                if (it.takerUuid == uuid) it.takerPrice else it.makerPrice,
                it.matchedQuantity,
                if (isMakerBuyer) makerOrder.quoteQuantity!! else takerOrder.quoteQuantity!!,
                if (it.takerUuid == uuid) it.takerCommission!! else it.makerCommission!!,
                if (it.takerUuid == uuid) it.takerCommissionAsset!! else it.makerCommissionAsset!!,
                Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                if (it.takerUuid == uuid)
                    OrderDirection.ASK == takerOrder.direction
                else
                    OrderDirection.ASK == makerOrder.direction,
                it.makerUuid == uuid,
                true,
                isMakerBuyer
            )
        }.toList()
    }

    override suspend fun txOfTrades(transactionRequest: TransactionRequest): TransactionResponse? {

        if (transactionRequest.ascendingByTime == true)
            return TransactionResponse(
                tradeRepository.findTxOfTradesAsc(
                    transactionRequest.owner!!,
                    transactionRequest.startTime?.let {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(transactionRequest.startTime!!),
                            ZoneId.systemDefault()
                        )
                    }
                        ?: null,
                    transactionRequest.endTime?.let {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(transactionRequest.endTime!!),
                            ZoneId.systemDefault()
                        )
                    }
                        ?: null,
                    transactionRequest.offset, transactionRequest.limit
                ).map { it.toDto() }.collectList()?.awaitFirstOrNull()
            )
        else
            return TransactionResponse(
                tradeRepository.findTxOfTradesDesc(
                    transactionRequest.owner!!,
                    transactionRequest.startTime?.let {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(transactionRequest.startTime!!),
                            ZoneId.systemDefault()
                        )
                    }
                        ?: null,
                    transactionRequest.endTime?.let {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(transactionRequest.endTime!!),
                            ZoneId.systemDefault()
                        )
                    }
                        ?: null,
                    transactionRequest.offset, transactionRequest.limit
                ).map { it.toDto() }.collectList()?.awaitFirstOrNull()
            )
    }

    override suspend fun getOrderHistory(
        uuid: String,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        orderType: MatchingOrderType?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<OrderData> {
        return orderRepository.findByCriteria(
            uuid,
            symbol,
            startTime,
            endTime,
            orderType,
            direction,
            limit,
            offset,
        ).toList()
    }

    override suspend fun getTradeHistory(
        uuid: String,
        symbol: String?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        direction: OrderDirection?,
        limit: Int?,
        offset: Int?,
    ): List<Trade> {
        return tradeRepository.findByCriteria(
            uuid,
            symbol,
            startTime,
            endTime,
            direction,
            limit,
            offset
        ).toList()
    }
}