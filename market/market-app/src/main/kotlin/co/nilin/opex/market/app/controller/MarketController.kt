package co.nilin.opex.market.app.controller

import co.nilin.opex.market.app.data.CountResponse
import co.nilin.opex.market.app.utils.asLocalDateTime
import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.*

@RestController
@RequestMapping("/v1/market")
class MarketController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/ticker")
    suspend fun priceChangeSince(@RequestParam since: Long): List<PriceChange> {
        return marketQueryHandler.getTradeTickerData(since.asLocalDateTime())
    }

    @GetMapping("/{symbol}/ticker")
    suspend fun priceChangeForSymbolSince(
        @PathVariable symbol: String,
        @RequestParam since: Long
    ): PriceChange? {
        return marketQueryHandler.getTradeTickerDateBySymbol(symbol, since.asLocalDateTime())
    }

    @GetMapping("/{symbol}/order-book")
    suspend fun getOrderBookForSymbol(
        @PathVariable symbol: String,
        @RequestParam direction: OrderDirection,
        @RequestParam(required = false) limit: Int = 500
    ): List<OrderBook> {
        return if (direction == OrderDirection.BID)
            marketQueryHandler.openBidOrders(symbol, limit)
        else
            marketQueryHandler.openAskOrders(symbol, limit)
    }

    @GetMapping("/{symbol}/recent-trades")
    suspend fun getRecentTradesForSymbol(
        @PathVariable symbol: String,
        @RequestParam(required = false) limit: Int = 500
    ): List<MarketTrade> {
        return marketQueryHandler.recentTrades(symbol, limit)
    }

    @GetMapping("/{symbol}/last-order")
    suspend fun getLastOrderForSymbol(@PathVariable symbol: String): Order? {
        return marketQueryHandler.lastOrder(symbol)
    }

    @GetMapping("/prices")
    suspend fun getLastPriceForSymbol(@RequestParam(required = false) symbol: String?): List<PriceTicker> {
        return marketQueryHandler.lastPrice(symbol)
    }

    @GetMapping("/best-prices")
    suspend fun getOrderBookForSymbol(@RequestParam symbols: List<String>): List<BestPrice> {
        return marketQueryHandler.getBestPriceForSymbols(symbols)
    }

    @GetMapping("/active-users")
    suspend fun getNumberOfActiveUsers(@RequestParam interval: Long): CountResponse {
        val active = marketQueryHandler.numberOfActiveUsers(interval.asLocalDateTime())
        return CountResponse(active)
    }

    @GetMapping("/orders-count")
    suspend fun getNumberOfOrders(
        @RequestParam interval: Long,
        @RequestParam(required = false) symbol: String?
    ): CountResponse {
        val count = marketQueryHandler.numberOfOrders(interval.asLocalDateTime(), symbol)
        return CountResponse(count)
    }

    @GetMapping("/trades-count")
    suspend fun getNumberOfTrades(
        @RequestParam interval: Long,
        @RequestParam(required = false) symbol: String?
    ): CountResponse {
        val count = marketQueryHandler.numberOfTrades(interval.asLocalDateTime(), symbol)
        return CountResponse(count)
    }

}