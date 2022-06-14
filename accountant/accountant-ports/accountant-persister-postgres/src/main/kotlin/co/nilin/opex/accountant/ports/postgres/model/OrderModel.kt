package co.nilin.opex.accountant.ports.postgres.model

import co.nilin.opex.matching.engine.core.model.MatchConstraint
import co.nilin.opex.matching.engine.core.model.OrderDirection
import co.nilin.opex.matching.engine.core.model.OrderType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
data class OrderModel(
    @Id var id: Long?,
    val ouid: String,
    val uuid: String,
    val pair: String,
    @Column(value = "matching_engine_id") val matchingEngineId: Long?,
    @Column("maker_fee") val makerFee: BigDecimal,
    @Column("taker_fee") val takerFee: BigDecimal,
    @Column("left_side_fraction") val leftSideFraction: BigDecimal,
    @Column("right_side_fraction") val rightSideFraction: BigDecimal,
    @Column("user_level") val userLevel: String,
    @Column("direction") val direction: OrderDirection,
    @Column("match_constraint") val matchConstraint: MatchConstraint,
    @Column("order_type") val orderType: OrderType,
    @Column("price") val price: Long,
    @Column("quantity") val quantity: Long,
    @Column("filled_quantity") val filledQuantity: Long,
    @Column("orig_price") val origPrice: BigDecimal,
    @Column("orig_quantity") val origQuantity: BigDecimal,
    @Column("filled_orig_quantity") val filledOrigQuantity: BigDecimal,
    @Column("first_transfer_amount") val firstTransferAmount: BigDecimal,
    @Column("remained_transfer_amount") val remainedTransferAmount: BigDecimal,
    @Column("status") val status: Int,
    val agent: String,
    val ip: String,
    @Column("create_date") val createDate: LocalDateTime
)