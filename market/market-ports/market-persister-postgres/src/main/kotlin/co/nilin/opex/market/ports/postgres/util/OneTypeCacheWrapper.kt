package co.nilin.opex.market.ports.postgres.util

import org.springframework.cache.CacheManager
import java.time.LocalDateTime

class OneTypeCacheWrapper<T>(private val manager: CacheManager, private val cacheName: String) {

    private val cache = manager.getCache(cacheName) ?: throw IllegalStateException("Cache $cacheName not found")

    fun put(key: Any, value: T) {
        cache.put(key, value)
    }

    fun putIfAbsent(key: Any, value: T) {
        cache.putIfAbsent(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun get(key: Any): T? {
        val value = cache.get(key) ?: return null
        val item = value.get() ?: return null

        if (item is TimedCacheItem<*>) {
            if (item.evictionTime.isBefore(LocalDateTime.now()))
                cache.evict(key)
            return item.cacheValue as T
        }

        return item as T
    }

    fun getOrElse(key: Any, put: Boolean = true, action: () -> T): T {
        val value = get(key)
        if (value != null)
            return value

        val item = action()
        if (put) put(key, item)
        return item
    }

    fun putTimeBased(key: Any, value: T, evictionTime: LocalDateTime) {
        val timedCacheItem = TimedCacheItem(value, evictionTime)
        cache.putIfAbsent(key, timedCacheItem)
    }

}