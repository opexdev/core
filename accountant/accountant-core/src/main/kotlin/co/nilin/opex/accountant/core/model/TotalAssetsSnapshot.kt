package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Currency

data class TotalAssetsSnapshot(
    val uuid: String,
    val totalAmount: BigDecimal,
    val quoteCurrency: Currency,
    val snapshotDate: LocalDateTime,
)