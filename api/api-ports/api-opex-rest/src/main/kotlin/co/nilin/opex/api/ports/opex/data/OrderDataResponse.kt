package co.nilin.opex.api.ports.opex.data

import co.nilin.opex.api.core.inout.MatchingOrderType
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class OrderDataResponse(
    val symbol: String,
    val orderId: Long,
    val orderType: MatchingOrderType,
    val side: OrderDirection,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal,
    val executedQuantity: BigDecimal,
    val takerFee: BigDecimal,
    val makerFee: BigDecimal,
    val status: OrderStatus,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
)