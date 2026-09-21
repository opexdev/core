package co.nilin.opex.price.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("rate_history")
data class RateHistoryModel(
    @Id val id: Long? = null,
    @Column("symbol") val symbol: String,
    @Column("price") val price: BigDecimal,
    @Column("created_date") val createdDate: LocalDateTime,
    @Column("source") val source: String
)
