package co.nilin.opex.price.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("pair_rate_config")
data class PairRateConfigModel(
    @Id val id: Int? = null,
    @Column("symbol") val symbol: String,
    @Column("strategy") val strategy: String?,
    @Column("margin") val margin: BigDecimal?,
    @Column("is_active") val isActive: Boolean,
    @Column("price_mode") val priceMode: String
)
