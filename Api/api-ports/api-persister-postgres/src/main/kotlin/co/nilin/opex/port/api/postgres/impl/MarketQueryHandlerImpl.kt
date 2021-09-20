package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.api.core.spi.SymbolMapper
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
import java.lang.Exception
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.math.max
import kotlin.math.min

@Component
class MarketQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
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
                    if (isMakerBuyer)
                        min(it.takerPrice, it.makerPrice).toString()
                    else
                        max(it.takerPrice, it.makerPrice).toString()
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
                    it.open ?: 0.0,
                    it.close ?: 0.0,
                    it.high ?: 0.0,
                    it.low ?: 0.0,
                    it.volume ?: 0.0,
                    0.0,
                    it.trades,
                    0.0,
                    0.0
                )
            }
    }

    private fun OrderModel.asQueryOrderResponse() = QueryOrderResponse(
        symbol,
        ouid,
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