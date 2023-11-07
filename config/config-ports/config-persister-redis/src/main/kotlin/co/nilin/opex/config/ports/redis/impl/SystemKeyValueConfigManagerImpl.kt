package co.nilin.opex.config.ports.redis.impl

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class SystemKeyValueConfigManagerImpl(
    @Qualifier("systemKeyValueConfigRedisTemplate")
    private val template: RedisTemplate<String, String>
) {

    private val key = "systemConfig"

    fun store(key: String, value: String) {
        put(key, value)
    }

    fun fetch(key: String): String? {
        return template.opsForHash<String, String>()
            .get(this.key, key)
    }

    private fun put(key: String, value: String) {
        template.opsForHash<String, String>()
            .put(this.key, key, value)
    }

}