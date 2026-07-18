package co.nilin.opex.market.ports.postgres.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class MarketTradeProjection(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val id: Long,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val createDate: LocalDateTime,
    val isMakerBuyer: Boolean
)