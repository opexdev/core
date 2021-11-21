package co.nilin.opex.eventlog.core.spi

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

interface EventPersister {
    suspend fun saveEvent(event: CoreEvent): List<Event>
}