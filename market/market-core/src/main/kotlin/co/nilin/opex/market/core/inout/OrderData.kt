package co.nilin.opex.market.core.inout

import java.math.BigDecimal
import java.util.*

data class OrderData(
    val createDate: Date,
    val symbol: String,
    val orderTyper: OrderType,
    val direction: OrderDirection,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val takerFee: BigDecimal,
    val makerFee: BigDecimal,
    val status: Int,
    val appearance: Int,
    val updateDate: Date,
)
