package co.nilin.opex.matching.engine.core.eventh

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

object NoOpOrderBookEventSink : OrderBookEventSink {

    override suspend fun emit(event: CoreEvent) {
        // Live books do not publish events directly.
    }
}