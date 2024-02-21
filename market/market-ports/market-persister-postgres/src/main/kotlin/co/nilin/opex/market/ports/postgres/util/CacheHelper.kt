package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.cache.CacheManager
import java.time.LocalDateTime

class CacheHelper(private val manager: CacheManager, private val cacheName: String) {

    private val cache = manager.getCache(cacheName) ?: throw IllegalStateException("Cache $cacheName not found")
    private val logger by LoggerDelegate()

    fun put(key: Any, value: Any) {
        logger.info("Putting cache with key $key")
        cache.put(key, value)
    }

    fun putIfAbsent(key: Any, value: Any) {
        cache.putIfAbsent(key, value)
    }

    fun get(key: Any): Any? {
        val value = cache.get(key) ?: return null
        val item = value.get() ?: return null

        if (item is TimedCacheItem<*>) {
            if (item.evictionTime.isBefore(LocalDateTime.now())) {
                cache.evict(key)
                logger.info("Cache $key evicted")
            }
            return item.cacheValue
        }

        logger.info("Read from cache with $key")
        return item
    }

    suspend fun getOrElse(key: Any, put: Boolean = true, action: suspend () -> Any): Any {
        val value = get(key)
        if (value != null)
            return value

        val item = action()
        if (put) put(key, item)
        return item
    }

    fun putTimeBased(key: Any, value: Any, evictionTime: LocalDateTime) {
        logger.info("Putting time based cache with key $key")
        val timedCacheItem = TimedCacheItem(value, evictionTime)
        cache.putIfAbsent(key, timedCacheItem)
    }

    fun putTimeBased(key: Any, value: Any, evictIn: DynamicInterval) {
        putTimeBased(key, value, evictIn.localDateTimeInFuture())
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any?> getTimeBasedOrElse(
        key: Any,
        evictionTime: LocalDateTime,
        put: Boolean = true,
        action: suspend () -> T
    ): T {
        val value = get(key)
        if (value != null)
            return value as T

        val item = action()
        if (put) putTimeBased(key, item!!, evictionTime)
        return item
    }

    suspend fun <T : Any?> getTimeBasedOrElse(
        key: Any,
        evictIn: DynamicInterval,
        put: Boolean = true,
        action: suspend () -> T
    ): T {
        return getTimeBasedOrElse(key, evictIn.localDateTimeInFuture(), put, action)
    }

}