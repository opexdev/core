package co.nilin.opex.matching.engine.core.eventh

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

class CollectingOrderBookEventSink : OrderBookEventSink {

    private val collectedEvents = mutableListOf<CoreEvent>()

    override suspend fun emit(event: CoreEvent) {
        collectedEvents += event
    }

    fun events(): List<CoreEvent> =
        collectedEvents.toList()
}