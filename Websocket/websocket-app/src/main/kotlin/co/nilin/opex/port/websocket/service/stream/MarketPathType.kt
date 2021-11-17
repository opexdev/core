package co.nilin.opex.port.websocket.service.stream

sealed class MarketPathType(val base: String) {

    class Depth(val symbol: String) : MarketPathType("/market/depth")
    class Candle(val symbol: String) : MarketPathType("/market/kline")
    class Ticker(val symbol: String, val duration: String) : MarketPathType("/market/ticker")

    companion object {
        fun isPartOfBases(path: String): Boolean {
            return false
        }
    }
}

fun String.startsWithAny(vararg list: String): Boolean {
    for (l in list)
        if (this.startsWith(l))
            return true
    return false
}