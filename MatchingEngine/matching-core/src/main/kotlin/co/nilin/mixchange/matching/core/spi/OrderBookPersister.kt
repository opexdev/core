package co.nilin.mixchange.matching.core.spi

import co.nilin.mixchange.matching.core.model.PersistentOrderBook

interface OrderBookPersister {
    suspend fun storeLastState(orderBook: PersistentOrderBook)
    suspend fun loadLastState(symbol: String): PersistentOrderBook?
}