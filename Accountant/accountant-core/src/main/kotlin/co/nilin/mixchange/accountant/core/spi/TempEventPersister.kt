package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.matching.core.eventh.events.CoreEvent

interface TempEventPersister {
    suspend fun saveTempEvent(ouid: String, event: CoreEvent)
    suspend fun loadTempEvents(ouid: String): List<CoreEvent>
    suspend fun removeTempEvents(ouid: String)
}