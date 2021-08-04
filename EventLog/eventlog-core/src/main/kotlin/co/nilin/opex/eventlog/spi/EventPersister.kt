package co.nilin.opex.eventlog.spi

import co.nilin.opex.matching.core.eventh.events.CoreEvent

interface EventPersister {
    suspend fun saveEvent(event: CoreEvent): List<Event>
}