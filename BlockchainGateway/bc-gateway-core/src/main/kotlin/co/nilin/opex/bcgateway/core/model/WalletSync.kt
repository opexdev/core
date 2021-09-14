package co.nilin.opex.bcgateway.core.model

import java.time.LocalDateTime

data class WalletSyncSchedule(val retryTime: LocalDateTime, val delay: Long, val batchSize: Long?)
data class WalletSyncRecord(
    val time: LocalDateTime, val success: Boolean, val error: String?, val deposit: List<Deposit>
)
