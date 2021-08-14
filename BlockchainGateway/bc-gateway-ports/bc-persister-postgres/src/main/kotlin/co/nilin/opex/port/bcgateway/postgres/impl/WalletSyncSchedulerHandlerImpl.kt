package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.WalletSyncSchedule
import co.nilin.opex.bcgateway.core.spi.WalletSyncSchedulerHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class WalletSyncSchedulerHandlerImpl: WalletSyncSchedulerHandler {
    override suspend fun fetchActiveSchedule(time: LocalDateTime): WalletSyncSchedule? {
        TODO("Not yet implemented")
    }

    override suspend fun prepareScheduleForNextTry(syncSchedule: WalletSyncSchedule, time: LocalDateTime) {
        TODO("Not yet implemented")
    }
}