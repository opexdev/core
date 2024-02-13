package co.nilin.opex.market.ports.postgres.util

import org.springframework.cache.CacheManager
import java.time.LocalDateTime

class CacheWrapper(private val manager: CacheManager, private val cacheName: String) {

    private val cache = manager.getCache(cacheName) ?: throw IllegalStateException("Cache $cacheName not found")

    fun put(key: Any, value: Any) {
        cache.put(key, value)
    }

    fun putIfAbsent(key: Any, value: Any) {
        cache.putIfAbsent(key, value)
    }

    fun get(key: Any): Any? {
        val value = cache.get(key) ?: return null
        val item = value.get() ?: return null

        if (item is TimedCacheItem<*>) {
            if (item.evictionTime.isBefore(LocalDateTime.now()))
                cache.evict(key)
            return item.cacheValue
        }

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
        val timedCacheItem = TimedCacheItem(value, evictionTime)
        cache.putIfAbsent(key, timedCacheItem)
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

}