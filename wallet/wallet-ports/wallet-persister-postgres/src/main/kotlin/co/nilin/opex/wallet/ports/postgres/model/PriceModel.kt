package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("price")
data class PriceModel(
    @Id
    val baseCurrency: String,
    val quoteCurrency: String,
    val price: BigDecimal,
    val updateDate: LocalDateTime,
)