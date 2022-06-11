package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.ports.postgres.model.OrderModel
import co.nilin.opex.market.ports.postgres.model.OrderStatusModel
import co.nilin.opex.market.ports.postgres.model.TradeTickerData
import co.nilin.opex.market.ports.postgres.util.*
import co.nilin.opex.market.core.inout.OrderBookResponse
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.core.inout.PriceChangeResponse
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.market.core.spi.SymbolMapper
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

@Component
class MarketQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val symbolMapper: SymbolMapper,
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
            .map { OrderBookResponse(it.price, it.quantity) }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBookResponse> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortAscendingByPrice(
            symbol,
            OrderDirection.ASK,
            limit,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { OrderBookResponse(it.price, it.quantity) }
    }

    override suspend fun lastOrder(symbol: String): QueryOrderResponse? {
        val order = orderRepository.findLastOrderBySymbol(symbol).awaitFirstOrNull() ?: return null
        val status = orderStatusRepository.findMostRecentByOUID(order.ouid).awaitFirstOrNull()
        return order.asQueryOrderResponse(status)
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
                    if (isMakerBuyer) it.makerPrice else it.takerPrice,
                    it.matchedQuantity,
                    if (isMakerBuyer)
                        makerOrder.quoteQuantity!!
                    else
                        takerOrder.quoteQuantity!!,
                    Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                    true,
                    isMakerBuyer
                )
            }
    }

    override suspend fun lastPrice(symbol: String?): List<PriceTickerResponse> {
        val list = if (symbol.isNullOrEmpty())
            tradeRepository.findAllGroupBySymbol()
        else
            tradeRepository.findBySymbolGroupBySymbol(symbol)
        return list.collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                val makerOrder = orderRepository.findByOuid(it.makerOuid).awaitFirst()
                val apiSymbol = try {
                    symbolMapper.map(it.symbol)
                } catch (e: Exception) {
                    it.symbol
                }
                val isMakerBuyer = makerOrder.direction == OrderDirection.BID
                PriceTickerResponse(
                    apiSymbol,
                    if (isMakerBuyer) it.takerPrice.min(it.makerPrice).toString()
                    else it.takerPrice.max(it.makerPrice).toString()
                )
            }

    }

    override suspend fun getCandleInfo(
        symbol: String,
        interval: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int
    ): List<CandleData> {
        val st = if (startTime == null)
            tradeRepository.findFirstByCreateDate().awaitFirstOrNull()?.createDate
                ?: LocalDateTime.now()
        else
            with(Instant.ofEpochMilli(startTime)) {
                LocalDateTime.ofInstant(this, ZoneId.systemDefault())
            }

        val et = if (endTime == null)
            tradeRepository.findLastByCreateDate().awaitFirstOrNull()?.createDate
                ?: LocalDateTime.now()
        else
            with(Instant.ofEpochMilli(endTime)) {
                LocalDateTime.ofInstant(this, ZoneId.systemDefault())
            }

        return tradeRepository.candleData(symbol, interval, st, et, limit)
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .map {
                CandleData(
                    it.openTime,
                    it.closeTime,
                    it.open ?: BigDecimal.ZERO,
                    it.close ?: BigDecimal.ZERO,
                    it.high ?: BigDecimal.ZERO,
                    it.low ?: BigDecimal.ZERO,
                    it.volume ?: BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    it.trades,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                )
            }
    }

    private fun OrderModel.asQueryOrderResponse(orderStatusModel: OrderStatusModel?) = QueryOrderResponse(
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

    private fun TradeTickerData.asPriceChangeResponse(openTime: Long, closeTime: Long) = PriceChangeResponse(
        symbol,
        priceChange ?: BigDecimal.ZERO,
        priceChangePercent ?: BigDecimal.ZERO,
        weightedAvgPrice ?: BigDecimal.ZERO,
        lastPrice ?: BigDecimal.ZERO,
        lastQty ?: BigDecimal.ZERO,
        bidPrice ?: BigDecimal.ZERO,
        askPrice ?: BigDecimal.ZERO,
        openPrice ?: BigDecimal.ZERO,
        highPrice ?: BigDecimal.ZERO,
        lowPrice ?: BigDecimal.ZERO,
        volume ?: BigDecimal.ZERO,
        openTime,
        closeTime,
        firstId ?: -1,
        lastId ?: -1,
        count ?: 0
    )
}
