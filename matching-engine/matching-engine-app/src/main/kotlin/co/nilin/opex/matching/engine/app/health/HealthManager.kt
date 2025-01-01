package co.nilin.opex.matching.engine.app.health

import co.nilin.opex.matching.engine.ports.kafka.listener.utils.EventListenerInfo
import co.nilin.opex.matching.engine.ports.kafka.listener.utils.KafkaInformation
import com.ecwid.consul.v1.ConsulClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Status
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(KafkaInformation::class.java)

@Component
class HealthManager(
    @Value("\${app.engine-id}")
    private val engineId: String,
    private val kafkaIndicator: KafkaHealthIndicator,
    private val redisIndicator: RedisHealthIndicator,
    private val eventInfo: EventListenerInfo,
    private val consulClient: ConsulClient,
) {

    @Scheduled(initialDelay = 5_000, fixedDelay = 30_000)
    fun checkHealth() {
        try {
            val lastOrderTime = eventInfo.lastProcessedOrderRequestEventTime
            consulClient.setKVValue("/health/matching-engine-$engineId/lastOrderTime", lastOrderTime.toString())

            val kafka = kafkaIndicator.health()
            val redis = redisIndicator.health()
            val isUp = kafka.status == Status.UP && redis.status == Status.UP
            consulClient.setKVValue("/health/matching-engine-$engineId/isUp", isUp.toString())

            logger.info("Checked status. result: isUp=$isUp, lastOrderTime=$lastOrderTime")
        } catch (e: Exception) {
            logger.error("Health check failed", e)
        }
    }
}



