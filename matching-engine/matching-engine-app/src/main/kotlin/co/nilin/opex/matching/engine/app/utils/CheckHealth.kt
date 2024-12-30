package co.nilin.opex.matching.engine.app.utils

import co.nilin.opex.matching.engine.ports.kafka.listener.utils.KafkaHealthIndicator
import co.nilin.opex.matching.engine.ports.redis.utils.RedisHealthIndicator
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class CheckHealth(
    private val redisHealthIndicator: RedisHealthIndicator,
    private val kafkaHealthIndicator: KafkaHealthIndicator
) {

    private val logger = LoggerFactory.getLogger(CheckHealth::class.java)

    @Scheduled(initialDelay = 20000, fixedDelay = 5000)
    fun check() {
        logger.info("HEALTH: kafka=${kafkaHealthIndicator.isHealthy()}, redis=${redisHealthIndicator.isHealthy()}")
    }

}