package co.nilin.opex.matching.engine.app.health

import co.nilin.opex.matching.engine.ports.redis.utils.RedisInformation
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class RedisHealthIndicator(private val redisInformation: RedisInformation) : HealthIndicator {

    override fun health(): Health {
        return if (redisInformation.isConnected())
            Health.up().build()
        else
            Health.down().build()
    }
}