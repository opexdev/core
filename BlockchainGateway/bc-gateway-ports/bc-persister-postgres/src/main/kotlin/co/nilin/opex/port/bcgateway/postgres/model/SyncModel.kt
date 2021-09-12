package co.nilin.opex.port.bcgateway.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("chain_sync_schedule")
data class SyncScheduleModel(
    @Id @Column("chain") val chain: String, @Column("retry_time") val retryTime: LocalDateTime, val delay: Long
)

@Table("chain_sync_record")
data class SyncRecordModel(
    @Id @Column("chain") val chain: String,
    val time: LocalDateTime,
    @Column("endpoint_url") val endpointUrl: String,
    @Column("latest_block") val latestBlock: Long?,
    val success: Boolean,
    val error: String?
)


