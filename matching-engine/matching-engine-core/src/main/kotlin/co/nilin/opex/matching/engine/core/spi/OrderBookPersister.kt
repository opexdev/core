package co.nilin.opex.matching.engine.core.spi

import co.nilin.opex.matching.engine.core.model.PersistentOrderBook

interface OrderBookPersister {
    suspend fun storeLastState(orderBook: PersistentOrderBook)
    suspend fun loadLastState(symbol: String): PersistentOrderBook?
}