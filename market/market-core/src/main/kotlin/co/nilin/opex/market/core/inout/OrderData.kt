package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Date

data class OrderData(
    val symbol: String,
    val orderType: MatchingOrderType,
    val side: OrderDirection,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val takerFee: BigDecimal,
    val makerFee: BigDecimal,
    val status: Int,
    val appearance: Int,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
)
