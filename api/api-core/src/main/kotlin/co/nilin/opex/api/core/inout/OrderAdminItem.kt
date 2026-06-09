package co.nilin.opex.api.core.inout

import java.math.BigDecimal
import java.time.LocalDateTime


data class OrderAdminItem(
    val symbol: String,
    val ouid: String,
    val orderType: MatchingOrderType?,
    val side: OrderDirection,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val quoteQuantity: BigDecimal?,
    val executedQuantity: BigDecimal?,
    val takerFee: BigDecimal?,
    val makerFee: BigDecimal?,
    val status: OrderStatus?,
    val appearance: Int?,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val uuid: String?,
    val ownerName: String? = null,
)
