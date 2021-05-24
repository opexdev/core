package co.nilin.mixchange.eventlog.spi

import co.nilin.mixchange.matching.core.eventh.events.CoreEvent

interface EventPersister {
    suspend fun saveEvent(event: CoreEvent): List<Event>
}