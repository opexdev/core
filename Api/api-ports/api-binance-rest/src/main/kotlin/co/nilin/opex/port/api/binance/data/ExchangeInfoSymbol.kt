package co.nilin.opex.port.api.binance.data

import co.nilin.opex.api.core.inout.OrderType

data class ExchangeInfoSymbol(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val baseAssetPrecision: Int,
    val quoteAsset: String,
    val quoteAssetPrecision: Int,
    val orderTypes: List<OrderType> = OrderType.activeTypes(),
    val icebergAllowed: Boolean = false,
    val ocoAllowed: Boolean = false,
    val isSpotTradingAllowed: Boolean = false,
    val isMarginTradingAllowed: Boolean = false,
    val filters: List<String> = emptyList(),
    val permissions: List<String> = listOf("SPOT")
)