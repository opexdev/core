package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.WalletSyncSchedule
import co.nilin.opex.bcgateway.core.spi.WalletSyncSchedulerHandler
import co.nilin.opex.port.bcgateway.postgres.dao.WalletSyncScheduleRepository
import co.nilin.opex.port.bcgateway.postgres.model.WalletSyncScheduleModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class WalletSyncSchedulerHandlerImpl(private val walletSyncScheduleRepository: WalletSyncScheduleRepository) :
    WalletSyncSchedulerHandler {
    override suspend fun fetchActiveSchedule(time: LocalDateTime): WalletSyncSchedule? {
        val dao = walletSyncScheduleRepository.findActiveSchedule(time).awaitSingleOrNull()
        return if (dao !== null) WalletSyncSchedule(dao.retryTime, dao.delay, dao.batchSize) else null
    }

    override suspend fun prepareScheduleForNextTry(syncSchedule: WalletSyncSchedule, time: LocalDateTime) {
        val dao = WalletSyncScheduleModel(1, time, syncSchedule.delay, syncSchedule.batchSize)
        walletSyncScheduleRepository.save(dao).awaitFirst()
    }
}
