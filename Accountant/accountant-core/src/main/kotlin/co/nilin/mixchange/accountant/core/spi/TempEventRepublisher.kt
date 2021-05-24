package co.nilin.mixchange.accountant.core.spi

import co.nilin.mixchange.matching.core.eventh.events.CoreEvent

interface TempEventRepublisher {
    suspend fun republish(events: List<CoreEvent>)
}