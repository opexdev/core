package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import java.time.LocalDateTime

interface SyncSchedulerHandler {
    suspend fun fetchActiveSchedules(time: LocalDateTime): List<ChainSyncSchedule>
    suspend fun prepareScheduleForNextTry(syncSchedule: ChainSyncSchedule, time: LocalDateTime)
}