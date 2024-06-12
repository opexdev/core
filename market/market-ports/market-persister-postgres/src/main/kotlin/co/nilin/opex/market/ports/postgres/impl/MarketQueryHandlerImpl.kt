package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.common.utils.Interval
import co.nilin.opex.common.utils.minutes
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.model.TradeTickerData
import co.nilin.opex.market.ports.postgres.util.CacheHelper
import co.nilin.opex.market.ports.postgres.util.asOrderDTO
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Component
class MarketQueryHandlerImpl(
    private val orderRepository: OrderRepository,
    private val tradeRepository: TradeRepository,
    private val orderStatusRepository: OrderStatusRepository,
    private val cacheHelper: CacheHelper,
    private val redisTemplate: RedisTemplate<String, Any>
) : MarketQueryHandler {

    //TODO merge order and status fetching in query

    override suspend fun getTradeTickerData(interval: Interval): List<PriceChange> {
        return cacheHelper.getTimeBasedOrElse("tradeTickerData:${interval.label}", 2.minutes()) {
            tradeRepository.tradeTicker(interval.getLocalDateTime())
                .collectList()
                .awaitFirstOrElse { emptyList() }
                .map { it.asPriceChangeResponse(Date().time, interval.getTime()) }
        }
    }

    override suspend fun getTradeTickerDateBySymbol(symbol: String, interval: Interval): PriceChange? {
        val cacheId = "tradeTickerData:$symbol:${interval.label}"
        return cacheHelper.getTimeBasedOrElse(cacheId, 2.minutes()) {
            tradeRepository.tradeTickerBySymbol(symbol, interval.getLocalDateTime())
                .awaitFirstOrNull()
                ?.asPriceChangeResponse(Date().time, interval.getTime())
        }
    }

    override suspend fun openBidOrders(symbol: String, limit: Int): List<OrderBook> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortDescendingByPrice(
            symbol,
            OrderDirection.BID,
            limit,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { OrderBook(it.price, it.quantity) }
    }

    override suspend fun openAskOrders(symbol: String, limit: Int): List<OrderBook> {
        return orderRepository.findBySymbolAndDirectionAndStatusSortAscendingByPrice(
            symbol,
            OrderDirection.ASK,
            limit,
            listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
        ).collectList()
            .awaitFirstOrElse { emptyList() }
            .map { OrderBook(it.price, it.quantity) }
    }

    // @Cacheable does not support suspended functions. Spring 6.1 required.
    // @Cacheable(cacheNames = ["marketCache"], key = "'lastOrder'")
    override suspend fun lastOrder(symbol: String): Order? {
        val orderCache = cacheHelper.get("lastOrder")
        if (orderCache != null && orderCache is Order?)
            return orderCache

        val order = orderRepository.findLastOrderBySymbol(symbol).awaitFirstOrNull() ?: return null
        val status = orderStatusRepository.findMostRecentByOUID(order.ouid).awaitFirstOrNull()
        return order.asOrderDTO(status).also { cacheHelper.put("lastOrder", it) }
    }

    //TODO need better query
    override suspend fun recentTrades(symbol: String, limit: Int): List<MarketTrade> {
        val ops = redisTemplate.opsForList()
        val recentTradesCache = ops.range("recentTrades", 0, -1)
        if (recentTradesCache?.isNotEmpty() == true)
            return recentTradesCache as List<MarketTrade>

        return tradeRepository.findBySymbolSortDescendingByCreateDate(symbol, limit)
            .map {
                val takerOrder = orderRepository.findByOuid(it.takerOuid).awaitFirst()
                val makerOrder = orderRepository.findByOuid(it.makerOuid).awaitFirst()
                val isMakerBuyer = makerOrder.direction == OrderDirection.BID
                MarketTrade(
                    it.symbol,
                    it.baseAsset,
                    it.quoteAsset,
                    it.tradeId,
                    it.matchedPrice,
                    it.matchedQuantity,
                    if (isMakerBuyer)
                        makerOrder.quoteQuantity!!
                    else
                        takerOrder.quoteQuantity!!,
                    Date.from(it.createDate.atZone(ZoneId.systemDefault()).toInstant()),
                    true,
                    isMakerBuyer
                )
            }.toList()
            .onEach { ops.rightPush("recentTrades", it) }
            .also { redisTemplate.expireAt("recentTrades", 60.minutes().dateInFuture()) }
    }

    override suspend fun lastPrice(symbol: String?): List<PriceTicker> {
        val list = cacheHelper.getTimeBasedOrElse("lastPrice", 1.minutes()) {
            if (symbol.isNullOrEmpty())
                tradeRepository.findAllGroupBySymbol().collectList().awaitFirstOrElse { emptyList() }
            else
                tradeRepository.findBySymbolGroupBySymbol(symbol).collectList().awaitFirstOrElse { emptyList() }
        }
        return list.map { PriceTicker(it.symbol, it.matchedPrice.toString()) }
    }

    override suspend fun getBestPriceForSymbols(symbols: List<String>): List<BestPrice> {
        val id = symbols.joinToString { it }
        return cacheHelper.getTimeBasedOrElse("bestPricesForSymbols:$id", 1.minutes()) {
            tradeRepository.bestAskAndBidPrice(symbols)
                .collectList()
                .awaitFirstOrElse { emptyList() }
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
            tradeRepository.findFirstByCreateDate().awaitFirstOrNull()?.createDate ?: LocalDateTime.now()
        else
            with(Instant.ofEpochMilli(startTime)) {
                LocalDateTime.ofInstant(this, ZoneId.systemDefault())
            }

        val et = if (endTime == null)
            tradeRepository.findLastByCreateDate().awaitFirstOrNull()?.createDate ?: LocalDateTime.now()
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

    override suspend fun numberOfActiveUsers(interval: Interval): Long {
        return cacheHelper.getTimeBasedOrElse("activeUsers:${interval.label}", 5.minutes()) {
            orderRepository.countUsersWhoMadeOrder(interval.getLocalDateTime()).singleOrNull() ?: 0L
        }
    }

    override suspend fun numberOfTrades(interval: Interval, pair: String?): Long {
        return if (pair != null)
            cacheHelper.getTimeBasedOrElse("tradeCount:$pair:${interval.label}", 5.minutes()) {
                tradeRepository.countBySymbolNewerThan(interval.getLocalDateTime(), pair).singleOrNull() ?: 0
            }
        else
            cacheHelper.getTimeBasedOrElse("tradeCount:${interval.label}", 5.minutes()) {
                tradeRepository.countNewerThan(interval.getLocalDateTime()).singleOrNull() ?: 0
            }
    }

    override suspend fun numberOfOrders(interval: Interval, pair: String?): Long {
        return if (pair != null)
            cacheHelper.getTimeBasedOrElse("orderCount:$pair:${interval.label}", 5.minutes()) {
                orderRepository.countBySymbolNewerThan(interval.getLocalDateTime(), pair).singleOrNull() ?: 0
            }
        else
            cacheHelper.getTimeBasedOrElse("orderCount:${interval.label}", 5.minutes()) {
                orderRepository.countNewerThan(interval.getLocalDateTime()).singleOrNull() ?: 0
            }
    }

    override suspend fun mostIncreasePrice(interval: Interval, limit: Int): List<PriceStat> {
        return cacheHelper.getTimeBasedOrElse("priceIncrease:${interval.label}", 1.minutes()) {
            tradeRepository.findByMostIncreasedPrice(interval.getLocalDateTime(), limit)
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }.take(limit)
    }

    override suspend fun mostDecreasePrice(interval: Interval, limit: Int): List<PriceStat> {
        return cacheHelper.getTimeBasedOrElse("priceDecrease:${interval.label}", 1.minutes()) {
            tradeRepository.findByMostDecreasedPrice(interval.getLocalDateTime(), limit)
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }.take(limit)
    }

    override suspend fun mostVolume(interval: Interval): TradeVolumeStat? {
        return cacheHelper.getTimeBasedOrElse("mostVolume:${interval.label}", 1.minutes()) {
            tradeRepository.findByMostVolume(interval.getLocalDateTime()).awaitSingleOrNull()
        }
    }

    override suspend fun mostTrades(interval: Interval): TradeVolumeStat? {
        return cacheHelper.getTimeBasedOrElse("mostTrades:${interval.label}", 1.minutes()) {
            tradeRepository.findByMostTrades(interval.getLocalDateTime()).awaitSingleOrNull()
        }
    }

    private fun TradeTickerData.asPriceChangeResponse(openTime: Long, closeTime: Long) = PriceChange(
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
