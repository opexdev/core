package co.nilin.opex.bcgateway.ports.postgres.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("chain_sync_schedules")
data class ChainSyncScheduleModel(
    @Id
    @Column("chain")
    val chain: String,
    @Column("retry_time")
    val retryTime: LocalDateTime,
    var delay: Long,
    @Column("error_delay")
    var errorDelay: Long
)

@Table("chain_sync_records")
data class ChainSyncRecordModel(
    @Id @Column("chain") val chain: String,
    val time: LocalDateTime,
    @Column("endpoint_url") val endpointUrl: String,
    @Column("latest_block") val latestBlock: Long?,
    val success: Boolean,
    val error: String?
)
