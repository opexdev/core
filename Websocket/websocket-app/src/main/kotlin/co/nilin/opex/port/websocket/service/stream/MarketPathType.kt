package co.nilin.opex.port.websocket.service.stream

enum class MarketPathType(val base: String) {

    Depth("/market/depth"),
    Candle("/market/kline"),
    Ticker("/market/ticker");

    companion object {
        fun isValidPath(path: String): Boolean {
            return values().find { path.startsWith(it.base) } != null
        }
    }

}