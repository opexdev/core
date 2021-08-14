package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import co.nilin.opex.bcgateway.core.spi.SyncSchedulerHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SyncSchedulerHandlerImpl: SyncSchedulerHandler {
    override suspend fun fetchActiveSchedules(time: LocalDateTime): List<ChainSyncSchedule> {
        TODO("Not yet implemented")
    }

    override suspend fun prepareScheduleForNextTry(syncSchedule: ChainSyncSchedule, time: LocalDateTime) {
        TODO("Not yet implemented")
    }
}