package co.nilin.opex.matching.engine.core.eventh

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent

fun interface OrderBookEventSink {
    suspend fun emit(event: CoreEvent)
}