package co.nilin.opex.port.websocket.controller

import co.nilin.opex.port.websocket.service.MarketDestinationType
import co.nilin.opex.port.websocket.service.MarketStreamHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller

@Controller
class MarketController(private val handler: MarketStreamHandler) {

    data class OrderBookRequest(val symbol: String)
    data class PriceTickerRequest(val symbol: String)
    data class OverviewTickerRequest(val symbol: String, val duration: String)
    data class CandleTickerRequest(val symbol: String, val interval: String)

    @MessageMapping("/market/depth")
    fun requestOrderBook(@Payload request: OrderBookRequest) {
        handler.newSubscription(MarketDestinationType.Depth(request.symbol))
    }

    @MessageMapping("/market/price")
    fun requestPrice(@Payload request: PriceTickerRequest) {
        handler.newSubscription(MarketDestinationType.Price(request.symbol))
    }

    @MessageMapping("/market/overview")
    fun requestOverview(@Payload request: OverviewTickerRequest) {
        handler.newSubscription(MarketDestinationType.Overview(request.symbol, request.duration))
    }

    @MessageMapping("/market/kline")
    fun requestCandleData(@Payload request: CandleTickerRequest) {
        handler.newSubscription(MarketDestinationType.Candle(request.symbol, request.interval))
    }

}