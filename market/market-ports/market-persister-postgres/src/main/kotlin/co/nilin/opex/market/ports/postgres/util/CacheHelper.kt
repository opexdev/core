package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.cache.CacheManager

class CacheHelper(private val manager: CacheManager, private val cacheName: String) {

    private val cache = manager.getCache(cacheName) ?: throw IllegalStateException("Cache $cacheName not found")
    private val logger by LoggerDelegate()

    fun put(key: Any, value: Any?) {
        logger.info("Putting cache with key $key")
        cache.put(key, CacheValueWrapper(value))
    }

    fun putIfAbsent(key: Any, value: Any?) {
        cache.putIfAbsent(key, CacheValueWrapper(value))
    }

    fun evict(key: Any) {
        cache.evict(key)
    }

    fun get(key: Any): Any? {
        val value = cache.get(key) ?: return null
        val item = value.get() ?: return null

        if (item is CacheValueWrapper) {
            if (item.checkTimeToEvict()) {
                cache.evict(key)
                logger.info("Cache $key evicted")
            }
            return item.value
        }

        logger.info("Read from cache with $key")
        return item
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any?> getOrElse(key: Any, put: Boolean = true, action: suspend () -> T): T {
        val value = get(key)
        if (value != null)
            return value as T

        val item = action()
        if (put) put(key, item)
        return item
    }

    fun putTimeBased(key: Any, value: Any?, evictionTime: Long, ignoreNullOrEmpty: Boolean = true) {
        logger.info("Putting time based cache with key $key")
        // Do not put if item is a Collection and is empty
        if (value == null || (value is Collection<*> && ignoreNullOrEmpty && value.isEmpty())) return
        val cacheValueWrapper = CacheValueWrapper(value, evictionTime)
        cache.putIfAbsent(key, cacheValueWrapper)
    }

    fun putTimeBased(key: Any, value: Any, evictIn: DynamicInterval, ignoreNullOrEmpty: Boolean = true) {
        putTimeBased(key, value, evictIn.timeInFuture(), ignoreNullOrEmpty)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any?> getTimeBasedOrElse(
        key: Any,
        evictionTime: Long,
        put: Boolean = true,
        ignoreNullOrEmpty: Boolean = true,
        action: suspend () -> T
    ): T {
        val value = get(key)
        if (value != null)
            return value as T

        val item = action()
        if (put) putTimeBased(key, item, evictionTime, ignoreNullOrEmpty)
        return item
    }

    suspend fun <T : Any?> getTimeBasedOrElse(
        key: Any,
        evictIn: DynamicInterval,
        put: Boolean = true,
        ignoreNullOrEmpty: Boolean = true,
        action: suspend () -> T
    ): T {
        return getTimeBasedOrElse(key, evictIn.timeInFuture(), put, ignoreNullOrEmpty, action)
    }

}