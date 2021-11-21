package co.nilin.opex.api.core.inout

data class PriceChangeResponse(
    val symbol: String,
    val priceChange: Double = 0.0,
    val priceChangePercent: Double = 0.0,
    val weightedAvgPrice: Double = 0.0,
    val lastPrice: Double = 0.0,
    val lastQty: Double = 0.0,
    val bidPrice: Double = 0.0,
    val askPrice: Double = 0.0,
    val openPrice: Double = 0.0,
    val highPrice: Double = 0.0,
    val lowPrice: Double = 0.0,
    val volume: Double = 0.0,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long = 0,
    val lastId: Long = 0,
    val count: Long = 0,
)
