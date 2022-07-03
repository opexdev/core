package co.nilin.opex.market.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_rate")
data class CurrencyRateModel(
    @Id val id: Long? = null,
    val source: String,
    val destination: String,
    val rate: BigDecimal
)