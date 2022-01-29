package co.nilin.opex.accountant.core.inout

import co.nilin.opex.matching.engine.core.model.OrderDirection
import java.math.BigDecimal
import java.time.LocalDateTime

class RichTrade(
    val id: Long,
    val pair: String,
    val takerOuid: String,
    val takerUuid: String,
    val takerOrderId: Long,
    val takerDirection: OrderDirection,
    val takerPrice: BigDecimal,
    val takerQuantity: BigDecimal,
    val takerQuoteQuantity: BigDecimal,
    val takerRemainedQuantity: BigDecimal,
    val takerCommision: BigDecimal,
    val takerCommisionAsset: String,
    val makerOuid: String,
    val makerUuid: String,
    val makerOrderId: Long,
    val makerDirection: OrderDirection,
    val makerPrice: BigDecimal,
    val makerQuantity: BigDecimal,
    val makerQuoteQuantity: BigDecimal,
    val makerRemainedQuantity: BigDecimal,
    val makerCommision: BigDecimal,
    val makerCommisionAsset: String,
    val matchedQuantity: BigDecimal,
    val tradeDateTime: LocalDateTime
)