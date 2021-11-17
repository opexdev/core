package co.nilin.opex.port.websocket.controller

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller

@Controller
class MarketController {

    @SubscribeMapping("/market/depth/{symbol}")
    fun orderBook(@DestinationVariable("symbol") symbol: String): String {

        return "Subscribed"
    }

    @SubscribeMapping("/market/kline/{symbol}")
    fun candleData(@DestinationVariable("symbol") symbol: String): String {
        //TODO subscribe user to channel
        return "Subscribed"
    }

    @SubscribeMapping("/market/ticker/{symbol}-{duration}")
    fun priceChange(
        @DestinationVariable("symbol") symbol: String,
        @DestinationVariable("duration") duration: String
    ): String {
        //TODO subscribe user to channel
        return "Subscribed"
    }

}