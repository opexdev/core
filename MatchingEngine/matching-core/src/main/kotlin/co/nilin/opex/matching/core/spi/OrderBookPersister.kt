package co.nilin.opex.matching.core.spi

import co.nilin.opex.matching.core.model.PersistentOrderBook

interface OrderBookPersister {
    suspend fun storeLastState(orderBook: PersistentOrderBook)
    suspend fun loadLastState(symbol: String): PersistentOrderBook?
}