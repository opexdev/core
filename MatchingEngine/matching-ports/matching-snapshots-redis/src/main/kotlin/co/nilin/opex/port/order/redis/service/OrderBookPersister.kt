package co.nilin.opex.port.order.redis.service

import co.nilin.opex.matching.core.model.PersistentOrderBook
import co.nilin.opex.matching.core.spi.OrderBookPersister
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component

@Component
class OrderBookRedisPersister(
    @Qualifier("snapshotRedisTemplate")
    val redisTemplate: ReactiveRedisTemplate<String, PersistentOrderBook>
) : OrderBookPersister {

    override suspend fun storeLastState(orderBook: PersistentOrderBook) {
        redisTemplate.opsForHash<String, PersistentOrderBook>()
            .put("OrderbookSnapshots", orderBook.pair.toString(), orderBook)
            .subscribe()
    }

    override suspend fun loadLastState(symbol: String): PersistentOrderBook? =
        redisTemplate.opsForHash<String, PersistentOrderBook>()
            .get("OrderbookSnapshots", symbol)
            .blockOptional().orElse(null)

}
