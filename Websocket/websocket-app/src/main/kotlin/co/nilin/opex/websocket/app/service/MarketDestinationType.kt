package co.nilin.opex.websocket.app.service

sealed class MarketDestinationType(val base: String, val path: String) {

    data class Depth(val symbol: String) :
        MarketDestinationType("/market/depth", "/topic/market/depth/$symbol")

    object Price : MarketDestinationType("/market/price", "/topic/market/price")

    data class Overview(val symbol: String, val duration: String) :
        MarketDestinationType("/market/overview", "/topic/market/overview/$symbol-$duration")

    data class Candle(val symbol: String, val interval: String) :
        MarketDestinationType("/market/kline", "/topic/market/kline/$symbol-$interval")

    data class RecentTrades(val symbol: String) :
        MarketDestinationType("/market/recent-trades", "/topic/market/recent-trades/$symbol")
}