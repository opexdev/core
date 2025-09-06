package co.nilin.opex.accountant.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_rate")
data class CurrencyRateModel(
    val base: String,
    val quote: String,
    val rate: BigDecimal,
    @Id val id: Long? = null,
)