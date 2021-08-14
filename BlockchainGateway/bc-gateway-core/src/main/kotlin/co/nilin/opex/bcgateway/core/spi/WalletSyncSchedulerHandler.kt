package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.WalletSyncSchedule
import java.time.LocalDateTime

interface WalletSyncSchedulerHandler {
    suspend fun fetchActiveSchedule(time: LocalDateTime): WalletSyncSchedule?
    suspend fun prepareScheduleForNextTry(syncSchedule: WalletSyncSchedule, time: LocalDateTime)
}