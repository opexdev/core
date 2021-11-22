package co.nilin.opex.port.websocket.service

sealed class MarketDestinationType(val base: String, val path: String) {

    data class Depth(val symbol: String) :
        MarketDestinationType("/market/depth", "/topic/market/depth/$symbol")

    data class Price(val symbol: String) :
        MarketDestinationType("/market/price", "/topic/market/price/$symbol")

    data class Overview(val symbol: String, val duration: String) :
        MarketDestinationType("/market/overview", "/topic/market/overview/$symbol-$duration")

    data class Candle(val symbol: String, val interval: String) :
        MarketDestinationType("/market/kline", "/topic/market/kline/$symbol-$interval")
}