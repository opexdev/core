package co.nilin.opex.accountant.core.inout

import co.nilin.opex.matching.core.model.OrderDirection
import java.math.BigDecimal
import java.time.LocalDateTime

class RichTrade() {
    var id: Long = 0
    lateinit var pair: String
    lateinit var takerOuid: String
    lateinit var takerUuid: String
    var takerOrderId: Long = 0
    lateinit var takerDirection: OrderDirection
    lateinit var takerPrice: BigDecimal
    lateinit var takerQuantity: BigDecimal
    lateinit var takerQuoteQuantity: BigDecimal
    lateinit var takerRemainedQuantity: BigDecimal
    lateinit var takerCommision: BigDecimal
    lateinit var takerCommisionAsset: String
    lateinit var makerOuid: String
    lateinit var makerUuid: String
    var makerOrderId: Long = 0
    lateinit var makerDirection: OrderDirection
    lateinit var makerPrice: BigDecimal
    lateinit var makerQuantity: BigDecimal
    lateinit var makerQuoteQuantity: BigDecimal
    lateinit var makerRemainedQuantity: BigDecimal
    lateinit var matchedQuantity: BigDecimal
    lateinit var makerCommision: BigDecimal
    lateinit var makerCommisionAsset: String

    lateinit var tradeDateTime: LocalDateTime

    constructor(
            id: Long,
            pair: String,
            takerOuid: String,
            takerUuid: String,
            takerOrderId: Long,
            takerDirection: OrderDirection,
            takerPrice: BigDecimal,
            takerQuantity: BigDecimal,
            takerQuoteQuantity: BigDecimal,
            takerRemainedQuantity: BigDecimal,
            takerCommision: BigDecimal,
            takerCommisionAsset: String,
            makerOuid: String,
            makerUuid: String,
            makerOrderId: Long,
            makerDirection: OrderDirection,
            makerPrice: BigDecimal,
            makerQuantity: BigDecimal,
            makerQuoteQuantity: BigDecimal,
            makerRemainedQuantity: BigDecimal,
            makerCommision: BigDecimal,
            makerCommisionAsset: String,
            matchedQuantity: BigDecimal,
            tradeDateTime: LocalDateTime
    ) : this() {
        this.id = id
        this.pair = pair
        this.takerOuid = takerOuid
        this.takerUuid = takerUuid
        this.takerOrderId = takerOrderId
        this.takerDirection = takerDirection
        this.takerPrice = takerPrice
        this.takerQuantity = takerQuantity
        this.takerQuoteQuantity = takerQuoteQuantity
        this.takerRemainedQuantity = takerRemainedQuantity
        this.takerCommision = takerCommision
        this.takerCommisionAsset = takerCommisionAsset
        this.makerOuid = makerOuid
        this.makerUuid = makerUuid
        this.makerOrderId = makerOrderId
        this.makerDirection = makerDirection
        this.makerPrice = makerPrice
        this.makerQuantity = makerQuantity
        this.makerQuoteQuantity = makerQuoteQuantity
        this.makerRemainedQuantity = makerRemainedQuantity
        this.matchedQuantity = matchedQuantity
        this.makerCommision = makerCommision
        this.makerCommisionAsset = makerCommisionAsset
        this.tradeDateTime = tradeDateTime
    }
}