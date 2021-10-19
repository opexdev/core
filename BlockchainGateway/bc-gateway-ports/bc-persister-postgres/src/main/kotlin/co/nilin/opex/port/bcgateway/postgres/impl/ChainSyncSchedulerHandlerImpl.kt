package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import co.nilin.opex.bcgateway.core.spi.ChainSyncSchedulerHandler
import co.nilin.opex.port.bcgateway.postgres.dao.ChainSyncScheduleRepository
import co.nilin.opex.port.bcgateway.postgres.model.ChainSyncScheduleModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ChainSyncSchedulerHandlerImpl(private val chainSyncScheduleRepository: ChainSyncScheduleRepository) :
    ChainSyncSchedulerHandler {
    override suspend fun fetchActiveSchedules(time: LocalDateTime): List<ChainSyncSchedule> {
        return chainSyncScheduleRepository.findActiveSchedule(time).map {
            ChainSyncSchedule(it.chain, it.retryTime, it.delay)
        }.toList()
    }

    override suspend fun prepareScheduleForNextTry(syncSchedule: ChainSyncSchedule, time: LocalDateTime) {
        val dao = ChainSyncScheduleModel(syncSchedule.chainName, time, syncSchedule.delay)
        chainSyncScheduleRepository.save(dao).awaitFirst()
    }
}
