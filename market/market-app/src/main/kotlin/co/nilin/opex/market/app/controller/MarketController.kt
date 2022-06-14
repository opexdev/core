package co.nilin.opex.market.app.controller

import co.nilin.opex.market.core.inout.*
import co.nilin.opex.market.core.spi.MarketQueryHandler
import co.nilin.opex.utility.error.data.OpexError
import co.nilin.opex.utility.error.data.OpexException
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/market")
class MarketController(private val marketQueryHandler: MarketQueryHandler) {

    @GetMapping("/ticker")
    suspend fun priceChangeSince(@RequestParam since: LocalDateTime): List<PriceChange> {
        return marketQueryHandler.getTradeTickerData(since)
    }

    @GetMapping("/{symbol}/ticker")
    suspend fun priceChangeForSymbolSince(
        @PathVariable symbol: String,
        @RequestParam since: LocalDateTime
    ): PriceChange {
        return marketQueryHandler.getTradeTickerDateBySymbol(symbol, since)
            ?: throw OpexException(OpexError.PriceChangeNotFound)
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
    suspend fun getLastOrderForSymbol(@PathVariable symbol: String): Order {
        return marketQueryHandler.lastOrder(symbol) ?: throw OpexException(OpexError.LastOrderNotFound)
    }

    @GetMapping("/prices")
    suspend fun getLastPriceForSymbol(@RequestParam(required = false) symbol: String?): List<PriceTickerResponse> {
        return marketQueryHandler.lastPrice(symbol)
    }

}