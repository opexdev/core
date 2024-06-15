package co.nilin.opex.market.ports.postgres.util

import co.nilin.opex.common.utils.DynamicInterval
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class RedisCacheHelper(private val redisTemplate: RedisTemplate<String, Any>) {

    private val valueOps = redisTemplate.opsForValue()
    private val listOps = redisTemplate.opsForList()

    fun put(key: String, value: Any?, expireAt: DynamicInterval? = null) {
        value ?: return
        valueOps.set(key, value)
        expireAt?.let { redisTemplate.expireAt(key, it.dateInFuture()) }
    }

    fun putList(key: String, values: List<Any>, expireAt: DynamicInterval? = null) {
        // Why the fuck this doesn't work?
        // listOps.rightPushAll(key, values)
        values.forEach { listOps.rightPush(key, it) }
        expireAt?.let { redisTemplate.expireAt(key, it.dateInFuture()) }
    }

    fun putListItem(key: String, value: Any, rightPush: Boolean = true) {
        if (rightPush)
            listOps.rightPush(key, value)
        else
            listOps.leftPush(key, value)
    }

    fun <T> get(key: String): T? {
        return valueOps.get(key) as T
    }

    fun <T> getList(key: String): Collection<T>? {
        return listOps.range(key, 0, -1) as Collection<T>?
    }

    suspend fun <T : Any?> getOrElse(key: String, expireAt: DynamicInterval? = null, action: suspend () -> T): T {
        return if (hasKey(key))
            get(key)!!
        else {
            val value = action()
            put(key, value!!)
            expireAt?.let { setExpiration(key, it) }
            return value
        }
    }

    fun evict(key: String) {
        redisTemplate.delete(key)
    }

    fun setExpiration(key: String, interval: DynamicInterval) {
        redisTemplate.expireAt(key, interval.dateInFuture())
    }

    fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
}