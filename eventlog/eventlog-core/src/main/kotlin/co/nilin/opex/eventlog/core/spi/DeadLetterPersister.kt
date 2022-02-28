package co.nilin.opex.eventlog.core.spi

import co.nilin.opex.eventlog.core.inout.DeadLetterEvent

interface DeadLetterPersister {

    suspend fun save(event: DeadLetterEvent)

}