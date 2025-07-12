package co.nilin.opex.wallet.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("quote_currency")
data class QuoteCurrencyModel(
    @Id
    val id: Long? = null,
    val currency: String,
    val isActive: Boolean = false,
    var lastUpdateDate: LocalDateTime = LocalDateTime.now(),
)