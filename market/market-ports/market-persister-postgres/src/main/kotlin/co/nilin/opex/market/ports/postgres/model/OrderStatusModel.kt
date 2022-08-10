package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("order_status")
data class OrderStatusModel(
    val ouid: String,
    val executedQuantity: BigDecimal?,
    val accumulativeQuoteQty: BigDecimal?,
    val status: Int,
    val appearance: Int,
    val date: LocalDateTime = LocalDateTime.now(),
    @Id var id: Long? = null
)