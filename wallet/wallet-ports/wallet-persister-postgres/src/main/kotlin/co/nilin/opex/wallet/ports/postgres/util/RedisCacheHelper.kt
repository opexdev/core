package co.nilin.opex.wallet.ports.postgres.util

import co.nilin.opex.common.utils.DynamicInterval
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisCacheHelper(private val redisTemplate: RedisTemplate<String, Any>) {

    private val logger by LoggerDelegate()

    private val valueOps = redisTemplate.opsForValue()
    private val listOps = redisTemplate.opsForList()

    fun put(key: String, value: Any?, expireAt: DynamicInterval? = null) {
        value ?: return
        try {
            valueOps.set(key, value)
            expireAt?.let { redisTemplate.expireAt(key, it.dateInFuture()) }
        } catch (e: Exception) {
            logger.warn("Unable to put cache with key '$key'")
        }
    }

    fun putList(key: String, values: List<Any>, expireAt: DynamicInterval? = null) {
        try {
            values.forEach { listOps.rightPush(key, it) }
            expireAt?.let { redisTemplate.expireAt(key, it.dateInFuture()) }
        } catch (e: Exception) {
            logger.warn("Unable to put list cache with key '$key'")
        }
    }

    fun putListItem(key: String, value: Any, rightPush: Boolean = true) {
        try {
            if (rightPush)
                listOps.rightPush(key, value)
            else
                listOps.leftPush(key, value)
        } catch (e: Exception) {
            logger.warn("Unable to put list item cache with key '$key'")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return try {
            valueOps.get(key) as T
        } catch (e: Exception) {
            logger.warn("Unable to get cache value with key '$key'")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getList(key: String): Collection<T>? {
        return try {
            listOps.range(key, 0, -1) as Collection<T>?
        } catch (e: Exception) {
            logger.warn("Unable to get cache list with key '$key'")
            null
        }
    }

    suspend fun <T : Any?> getOrElse(key: String, expireAt: DynamicInterval? = null, action: suspend () -> T): T {
        val cacheValue = get<T>(key)
        return if (cacheValue != null)
            cacheValue
        else {
            val value = action()
            if (value != null) {
                put(key, value)
                expireAt?.let { setExpiration(key, it) }
            }
            return value
        }
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

    fun hasKey(key: String): Boolean {
        return try {
            redisTemplate.hasKey(key)
        } catch (e: Exception) {
            logger.warn("Unable fetch info of cache with key '$key'")
            false
        }
    }
}