package co.nilin.opex.accountant.core.utils

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class CacheManager<K, V> {

    private val cacheMap = ConcurrentHashMap<K, CacheEntry<V>>()

    data class CacheEntry<T>(
        val value: T,
        val timestamp: Long
    )

    fun put(key: K, value: V, expirationTime: Long, timeUnit: TimeUnit) {
        val expirationMillis = timeUnit.toMillis(expirationTime)
        cacheMap[key] = CacheEntry(value, System.currentTimeMillis() + expirationMillis)
    }

    fun get(key: K): V? {
        val entry = cacheMap[key]
        return if (entry != null && !isExpired(entry)) {
            entry.value
        } else {
            cacheMap.remove(key)
            null
        }
    }

    private fun isExpired(entry: CacheEntry<V>): Boolean {
        return System.currentTimeMillis() > entry.timestamp
    }

    fun remove(key: K) {
        cacheMap.remove(key)
    }

    fun clear() {
        cacheMap.clear()
    }
}