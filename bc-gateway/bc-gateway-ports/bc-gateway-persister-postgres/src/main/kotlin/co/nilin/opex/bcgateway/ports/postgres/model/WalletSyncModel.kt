package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("wallet_sync_schedules")
data class WalletSyncScheduleModel(
    @Id val id: Long?, val retryTime: LocalDateTime, val delay: Long, val batchSize: Long?
)

@Table("wallet_sync_records")
data class WalletSyncRecordModel(
    @Id val id: Long?,
    val time: LocalDateTime,
    val success: Boolean,
    val error: String?
)
