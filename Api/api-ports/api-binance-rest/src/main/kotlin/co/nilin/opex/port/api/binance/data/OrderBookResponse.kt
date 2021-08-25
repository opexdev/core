package co.nilin.opex.port.api.binance.data

import java.math.BigDecimal

data class OrderBookResponse(
    val lastUpdateId: Long,
    val bids: List<List<BigDecimal>>, // Inner list -> [0]: PRICE, [1]: QTY
    val asks: List<List<BigDecimal>> // Inner list -> [0]: PRICE, [1]: QTY
)