package co.nilin.opex.accountant.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TotalAssetsSnapshot(
    val uuid: String,
    val totalUSDT: BigDecimal,
    val totalIRT: BigDecimal,
    val snapshotDate: LocalDateTime,
)