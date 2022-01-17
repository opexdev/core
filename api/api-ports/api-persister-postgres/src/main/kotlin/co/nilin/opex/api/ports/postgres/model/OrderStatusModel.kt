package co.nilin.opex.api.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("order_status")
data class OrderStatusModel(
    val ouid: String,
    val executedQuantity: Double?,
    val accumulativeQuoteQty: Double?,
    val status: Int,
    val appearance:Int,
    val date: LocalDateTime = LocalDateTime.now(),
    @Id
    var id: Long? = null
)