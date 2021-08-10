package co.nilin.opex.port.accountant.postgres.impl

import co.nilin.opex.accountant.core.model.TempEvent
import co.nilin.opex.accountant.core.spi.TempEventPersister
import co.nilin.opex.matching.core.eventh.events.CoreEvent
import co.nilin.opex.port.accountant.postgres.dao.TempEventRepository
import co.nilin.opex.port.accountant.postgres.model.TempEventModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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

    override suspend fun removeTempEvents(tempEvents: List<TempEvent>) {
        tempEventRepository.deleteAll(tempEvents.map { event ->
            TempEventModel(
                event.id, event.ouid, event.eventBody.javaClass.name,
                "", event.eventDate
            )
        }).awaitFirstOrNull()
    }

    override suspend fun fetchTempEvents(
        offset: Long,
        size: Long
    ): List<TempEvent> {
        return tempEventRepository
            .findAll(PageRequest.of(offset.toInt(), size.toInt(), Sort.by(Sort.Direction.ASC, "eventDate")))
            .map { value: TempEventModel ->
                TempEvent(
                    value.id!!, value.ouid, Gson().fromJson(value.eventBody, Class.forName(value.eventType))
                            as
                            CoreEvent, value.eventDate
                )
            }
            .toList()
    }
}