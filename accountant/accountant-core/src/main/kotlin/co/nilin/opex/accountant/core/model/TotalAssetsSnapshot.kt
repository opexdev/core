package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TotalAssetsSnapshot(
    val uuid: String,
    val totalAmount: BigDecimal,
    val quoteCurrency: String,
    val snapshotDate: LocalDateTime,
)