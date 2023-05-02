package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("open_orders")
data class OpenOrderModel(
    val ouid: String,
    val executedQuantity: BigDecimal?,
    val status: Int,
    @Id
    val id: Long? = null,
)