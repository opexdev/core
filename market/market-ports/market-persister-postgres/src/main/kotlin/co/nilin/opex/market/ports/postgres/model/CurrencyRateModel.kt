package co.nilin.opex.market.ports.postgres.model

import co.nilin.opex.market.core.inout.RateSource
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

@Table("currency_rate")
data class CurrencyRateModel(
    @Id val id: Long? = null,
    val base: String,
    val quote: String,
    val source: RateSource,
    val rate: BigDecimal
)