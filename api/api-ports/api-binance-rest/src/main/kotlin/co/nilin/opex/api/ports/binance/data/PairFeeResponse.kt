package co.nilin.opex.api.ports.binance.data

data class PairFeeResponse(
    val symbol: String,
    val makerCommission: Double,
    val takerCommission: Double
)