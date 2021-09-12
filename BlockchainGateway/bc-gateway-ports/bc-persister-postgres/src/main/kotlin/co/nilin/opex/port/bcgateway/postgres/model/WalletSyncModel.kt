package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("wallet_sync_schedules")
data class WalletSyncScheduleModel(
    @Id val id: Long?, val retryTime: LocalDateTime, val delay: Long, val batchSize: Long?
)
