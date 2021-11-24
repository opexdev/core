package co.nilin.opex.websocket.app.controller

import co.nilin.opex.websocket.app.service.MarketDestinationType
import co.nilin.opex.websocket.app.service.MarketStreamHandler
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Controller
class MarketController(private val handler: MarketStreamHandler) {

    private val validDurations = arrayOf("24h", "7d", "1M")

    @SubscribeMapping("/market/depth/{symbol}")
    fun requestOrderBook(@DestinationVariable("symbol") symbol: String) {
        handler.newSubscription(MarketDestinationType.Depth(symbol))
    }

    @SubscribeMapping("/market/price")
    fun requestPrice() {
        handler.newSubscription(MarketDestinationType.Price)
    }

    @SubscribeMapping("/market/overview/{symbol}-{duration}")
    fun requestOverview(
        @DestinationVariable("symbol") symbol: String,
        @DestinationVariable("duration") duration: String
    ) {
        if (validDurations.contains(duration))
            handler.newSubscription(MarketDestinationType.Overview(symbol, duration))
    }

    @SubscribeMapping("/market/kline/{symbol}-{interval}")
    fun requestCandleData(
        @DestinationVariable("symbol") symbol: String,
        @DestinationVariable("interval") interval: String
    ) {
        handler.newSubscription(MarketDestinationType.Candle(symbol, interval))
    }

    @SubscribeMapping("/market/recent-trades/{symbol}")
    fun requestRecentTrades(@DestinationVariable("symbol") symbol: String) {
        handler.newSubscription(MarketDestinationType.RecentTrades(symbol))
    }

}