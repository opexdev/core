package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.inout.DeadLetterEvent
import co.nilin.opex.eventlog.core.spi.DeadLetterPersister
import co.nilin.opex.eventlog.ports.postgres.dao.DeadLetterEventRepository
import co.nilin.opex.eventlog.ports.postgres.model.DeadLetterEventModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class DeadLetterPersisterImpl(private val repository: DeadLetterEventRepository) : DeadLetterPersister {

    override suspend fun save(event: DeadLetterEvent) {
        repository.save(
            with(event) {
                DeadLetterEventModel(
                    originModule,
                    originTopic,
                    consumerGroup,
                    exceptionMessage,
                    exceptionStacktrace,
                    exceptionClassName,
                    value,
                    timestamp
                )
            }
        ).awaitFirstOrNull()
    }
}