package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.matching.core.model.OrderDirection
import co.nilin.opex.port.api.postgres.dao.OrderRepository
import co.nilin.opex.port.api.postgres.dao.TradeRepository
import co.nilin.opex.port.api.postgres.model.OrderModel
import co.nilin.opex.port.api.postgres.model.TradeTickerData
import co.nilin.opex.port.api.postgres.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

@Component
class MarketQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
) : MarketQueryHandler {

    override suspend fun getTradeTickerData(startFrom: LocalDateTime): List<PriceChangeResponse> {
        return tradeRepository.tradeTicker(startFrom)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map { it.asPriceChangeResponse(Date().time, startFrom.toInstant(ZoneOffset.UTC).toEpochMilli()) }

    }

    override suspend fun getTradeTickerDataBySymbol(symbol: String, startFrom: LocalDateTime): PriceChangeResponse {
        return tradeRepository.tradeTickerBySymbol(symbol, startFrom)
            .awaitFirstOrNull()
            ?.asPriceChangeResponse(Date().time, startFrom.toInstant(ZoneOffset.UTC).toEpochMilli())
            ?: PriceChangeResponse(
                symbol = symbol,
                openTime = Date().time,
                closeTime = startFrom.toInstant(ZoneOffset.UTC).toEpochMilli()
            )
    }

    override suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBookResponse> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortDescendingByPrice(
            symbol,
            OrderDirection.BID,
            limit,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { OrderBookResponse(it.price?.toBigDecimal(), it.quantity?.toBigDecimal()) }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBookResponse> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortAscendingByPrice(
            symbol,
            OrderDirection.ASK,
            limit,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { OrderBookResponse(it.price?.toBigDecimal(), it.quantity?.toBigDecimal()) }
    }

    override suspend fun lastOrder(symbol: String): QueryOrderResponse? {
        return orderRepository.findLastOrderBySymbol(symbol)
            .awaitFirstOrNull()
            ?.asQueryOrderResponse()
    }

    override suspend fun recentTrades(symbol: String, limit: Int): Flow<MarketTradeResponse> {
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

    private fun TradeTickerData.asPriceChangeResponse(openTime: Long, closeTime: Long) = PriceChangeResponse(
        symbol,
        priceChange ?: 0.0,
        priceChangePercent ?: 0.0,
        weightedAvgPrice ?: 0.0,
        lastPrice ?: 0.0,
        lastQty ?: 0.0,
        bidPrice ?: 0.0,
        askPrice ?: 0.0,
        openPrice ?: 0.0,
        highPrice ?: 0.0,
        lowPrice ?: 0.0,
        volume ?: 0.0,
        openTime,
        closeTime,
        firstId ?: -1,
        lastId ?: -1,
        count ?: 0
    )
}