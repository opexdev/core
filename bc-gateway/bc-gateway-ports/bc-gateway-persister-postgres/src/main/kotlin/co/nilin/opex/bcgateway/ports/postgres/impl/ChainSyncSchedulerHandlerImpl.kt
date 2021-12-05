package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncSchedule
import co.nilin.opex.bcgateway.core.spi.ChainSyncSchedulerHandler
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainSyncScheduleRepository
import co.nilin.opex.bcgateway.ports.postgres.model.ChainSyncScheduleModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class ChainSyncSchedulerHandlerImpl(private val chainSyncScheduleRepository: ChainSyncScheduleRepository) :
    ChainSyncSchedulerHandler {

    override suspend fun fetchActiveSchedules(time: LocalDateTime): List<ChainSyncSchedule> {
        return chainSyncScheduleRepository.findActiveSchedule(time).map {
            ChainSyncSchedule(it.chain, it.retryTime, it.delay, it.errorDelay)
        }.toList()
    }

    override suspend fun prepareScheduleForNextTry(syncSchedule: ChainSyncSchedule, success: Boolean) {
        val chain = syncSchedule.chainName
        val time = LocalDateTime.now().plus(
            if (success) syncSchedule.delay else syncSchedule.errorDelay,
            ChronoUnit.SECONDS
        )
        val dao = ChainSyncScheduleModel(chain, time, syncSchedule.delay, syncSchedule.errorDelay)
        chainSyncScheduleRepository.save(dao).awaitFirst()
    }
}
