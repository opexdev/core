package co.nilin.opex.price.app.cache

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

/**
 * Thin wrapper over [RedisTemplate], mirroring the market module's cache helper so the two
 * services cache in the same style. All operations fail soft: a Redis outage degrades to a
 * cache miss, never an error.
 */
@Component
class RedisCacheHelper(private val redisTemplate: RedisTemplate<String, Any>) {

    private val logger by LoggerDelegate()

    private val valueOps = redisTemplate.opsForValue()

    fun put(key: String, value: Any?, expireAt: DynamicInterval? = null) {
        value ?: return
        try {
            valueOps.set(key, value)
            expireAt?.let { redisTemplate.expireAt(key, it.dateInFuture()) }
        } catch (e: Exception) {
            logger.warn("Unable to put cache with key '$key'")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return try {
            valueOps.get(key) as T?
        } catch (e: Exception) {
            logger.warn("Unable to get cache value with key '$key'")
            null
        }
    }

    suspend fun <T : Any?> getOrElse(key: String, expireAt: DynamicInterval? = null, action: suspend () -> T): T {
        get<T>(key)?.let { return it }
        val value = action()
        if (value != null) {
            put(key, value)
            expireAt?.let { setExpiration(key, it) }
        }
        return value
    }

    fun evict(key: String) {
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            logger.warn("Unable to evict cache with key '$key'")
        }
    }

    fun setExpiration(key: String, interval: DynamicInterval) {
        try {
            redisTemplate.expireAt(key, interval.dateInFuture())
        } catch (e: Exception) {
            logger.warn("Unable to set expiration date for cache with key '$key'")
        }
    }
}
