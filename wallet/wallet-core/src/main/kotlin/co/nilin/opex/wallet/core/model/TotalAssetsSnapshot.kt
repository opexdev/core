package co.nilin.opex.wallet.core.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TotalAssetsSnapshot(
    val owner: Long,
    val totalUSDT: BigDecimal,
    val totalIRT: BigDecimal,
    val snapshotDate: LocalDateTime,
)
