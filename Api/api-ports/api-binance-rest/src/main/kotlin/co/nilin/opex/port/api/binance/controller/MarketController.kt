package co.nilin.opex.port.api.binance.controller

import co.nilin.opex.api.core.spi.MarketQueryHandler
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.port.api.binance.data.OrderBookResponse
import co.nilin.opex.api.core.inout.PriceChangeResponse
import co.nilin.opex.api.core.inout.PriceTickerResponse
import co.nilin.opex.port.api.binance.data.RecentTradeResponse
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.utility.error.data.throwError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@RestController
class MarketController(
    private val marketQueryHandler: MarketQueryHandler,
    private val symbolMapper: SymbolMapper,
) {

    private val orderBookValidLimits = arrayListOf(5, 10, 20, 50, 100, 500, 1000, 5000)
    private val validDurations = arrayListOf("24h", "7d", "1m")

    // Limit - Weight
    // 5, 10, 20, 50, 100 - 1
    // 500 - 5
    // 1000 - 10
    // 5000 - 50
    @GetMapping("/v3/depth")
    suspend fun orderBook(
        @RequestParam("symbol")
        symbol: String,
        @RequestParam("limit", required = false)
        limit: Int? // Default 100; max 5000. Valid limits:[5, 10, 20, 50, 100, 500, 1000, 5000]
    ): OrderBookResponse {
        val validLimit = limit ?: 100
        val localSymbol = symbolMapper.unmap(symbol) ?: throw OpexException(OpexError.SymbolNotFound)
        if (!orderBookValidLimits.contains(validLimit))
            throwError(OpexError.InvalidLimitForOrderBook)

        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketQueryHandler.openBidOrders(localSymbol, validLimit)
        val askOrders = marketQueryHandler.openAskOrders(localSymbol, validLimit)

        bidOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedBidOrders.add(mapped)
        }

        askOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedAskOrders.add(mapped)
        }

        val lastOrder = marketQueryHandler.lastOrder(localSymbol)
        return OrderBookResponse(lastOrder?.orderId ?: -1, mappedBidOrders, mappedAskOrders)
    }

    @GetMapping("/v3/trades")
    suspend fun recentTrades(
        principal: Principal,
        @RequestParam("symbol")
        symbol: String,
        @RequestParam("limit", required = false)
        limit: Int? // Default 500; max 1000.
    ): Flow<RecentTradeResponse> {
        val validLimit = limit ?: 500
        val localSymbol = symbolMapper.unmap(symbol) ?: throw OpexException(OpexError.SymbolNotFound)
        if (validLimit !in 1..1000)
            throwError(OpexError.InvalidLimitForRecentTrades)

        return marketQueryHandler.recentTrades(localSymbol, validLimit)
            .map {
                RecentTradeResponse(
                    it.id,
                    it.price,
                    it.qty,
                    it.quoteQty,
                    it.time.time,
                    it.isMakerBuyer,
                    it.isBestMatch
                )
            }
    }

    @GetMapping("/v3/ticker/{duration:24h|7d|1m}")
    suspend fun priceChange(
        @PathVariable("duration")
        duration: String,
        @RequestParam("symbol", required = false)
        symbol: String?,
    ): List<PriceChangeResponse> {
        val localSymbol = if (symbol.isNullOrEmpty())
            null
        else
            symbolMapper.unmap(symbol) ?: throw OpexException(OpexError.SymbolNotFound)

        if (!validDurations.contains(duration))
            throwError(OpexError.InvalidPriceChangeDuration)

        val now = Date().time
        val before = when (duration) {
            "24h" -> Date(now - TimeUnit.DAYS.toMillis(1))
            "7d" -> Date(now - TimeUnit.DAYS.toMillis(7))
            "1m" -> Date(now - TimeUnit.DAYS.toMillis(31))
            else -> Date(now - TimeUnit.DAYS.toMillis(1))
        }

        val instant = Instant.ofEpochMilli(before.time)
        val startDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        return if (symbol.isNullOrEmpty())
            marketQueryHandler.getTradeTickerData(startDate)
        else
            listOf(marketQueryHandler.getTradeTickerDataBySymbol(localSymbol!!, startDate))
    }

    // Weight
    // 1 for a single symbol
    // 2 when the symbol parameter is omitted
    @GetMapping("/v3/ticker/price")
    suspend fun priceTicker(@RequestParam("symbol", required = false) symbol: String?): List<PriceTickerResponse> {
        val localSymbol = if (symbol == null)
            null
        else
            symbolMapper.unmap(symbol) ?: throw OpexException(OpexError.SymbolNotFound)
        return marketQueryHandler.lastPrice(localSymbol)
    }

}