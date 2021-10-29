package co.nilin.opex.matching.core.eventh.events

import co.nilin.opex.matching.core.model.PersistentOrderBook

data class OrderBookPublishedEvent(val persistentOrderBook: PersistentOrderBook): CoreEvent()