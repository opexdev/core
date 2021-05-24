package co.nilin.mixchange.port.accountant.postgres.impl

import co.nilin.mixchange.accountant.core.spi.TempEventPersister
import co.nilin.mixchange.matching.core.eventh.events.CoreEvent
import co.nilin.mixchange.port.accountant.postgres.dao.TempEventRepository
import co.nilin.mixchange.port.accountant.postgres.model.TempEventModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TempEventPersisterImpl(val tempEventRepository: TempEventRepository) : TempEventPersister {
    override suspend fun saveTempEvent(ouid: String, event: CoreEvent) {
        tempEventRepository.save(
            TempEventModel(
                null, ouid, event.javaClass.name,
                Gson().toJson(event), LocalDateTime.now()
            )
        ).awaitSingle()
    }

    override suspend fun loadTempEvents(ouid: String): List<CoreEvent> {
        return tempEventRepository
            .findByOuid(ouid)
            .map { value: TempEventModel ->
                Gson().fromJson(value.eventBody, Class.forName(value.eventType)) as CoreEvent
            }
            .toList()
    }

    override suspend fun removeTempEvents(ouid: String) {
        tempEventRepository.deleteByOuid(ouid).awaitFirstOrNull()
    }
}