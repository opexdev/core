package co.nilin.opex.auth.utils

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheHelper(private val redisTemplate: ReactiveStringRedisTemplate) {

    private val valueOps = redisTemplate.opsForValue()

    suspend fun putIfAbsent(key: String, value: String, ttl: Duration): Boolean =
        valueOps.setIfAbsent(key, value, ttl).awaitSingle()

    suspend fun get(key: String): String? = valueOps.get(key).awaitSingleOrNull()

    suspend fun getTtl(key: String): Duration? =
        redisTemplate.getExpire(key).awaitSingleOrNull()?.takeIf { it.toMillis() > 0 }

    suspend fun evict(key: String) {
        redisTemplate.delete(key).awaitSingle()
    }
}
