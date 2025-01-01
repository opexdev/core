package co.nilin.opex.matching.engine.ports.redis.utils

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.stereotype.Component

@Component
class RedisInformation(private val factory: ReactiveRedisConnectionFactory) {

    private val logger = LoggerFactory.getLogger(RedisInformation::class.java)

    fun isConnected(): Boolean {
        return try {
            val connection = factory.reactiveConnection
            val pong = connection.ping().block()
            if (pong != null && pong == "PONG")
                return true

            logger.warn("Redis connection is not established")
            false
        } catch (e: Exception) {
            logger.error("Unable to check redis connection", e)
            false
        }
    }
}