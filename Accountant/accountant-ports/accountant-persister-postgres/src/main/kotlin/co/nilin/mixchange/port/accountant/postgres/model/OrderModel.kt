package co.nilin.mixchange.port.accountant.postgres.model


import co.nilin.mixchange.matching.core.model.OrderDirection
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("orders")
class OrderModel(
    @Id var id: Long?,
    val ouid: String,
    val uuid: String,
    val pair: String,
    @Column(value = "matching_engine_id") val matchingEngineId: Long?,
    @Column("maker_fee") val makerFee: Double,
    @Column("taker_fee") val takerFee: Double,
    @Column("left_side_fraction") val leftSideFraction: Double,
    @Column("right_side_fraction") val rightSideFraction: Double,
    @Column("user_level") val userLevel: String,
    @Column("direction") val direction: OrderDirection,
    @Column("price") val price: Long,
    @Column("quantity") val quantity: Long,
    @Column("filled_quantity") val filledQuantity: Long,
    @Column("first_transfer_amount") val firstTransferAmount: BigDecimal,
    @Column("remained_transfer_amount") val remainedTransferAmount: BigDecimal,
    @Column("status") val status: Int,
    val agent: String,
    val ip: String,
    @Column("create_date") val createDate: LocalDateTime
)