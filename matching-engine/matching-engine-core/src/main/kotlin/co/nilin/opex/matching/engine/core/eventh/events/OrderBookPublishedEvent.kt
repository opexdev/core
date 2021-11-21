package co.nilin.opex.matching.engine.core.eventh.events

import co.nilin.opex.matching.engine.core.model.PersistentOrderBook

data class OrderBookPublishedEvent(val persistentOrderBook: PersistentOrderBook) : CoreEvent()