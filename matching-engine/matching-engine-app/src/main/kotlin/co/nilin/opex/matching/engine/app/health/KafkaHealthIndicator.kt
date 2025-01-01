package co.nilin.opex.matching.engine.app.health

import co.nilin.opex.matching.engine.ports.kafka.listener.utils.KafkaInformation
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

@Component
class KafkaHealthIndicator(private val kafkaInformation: KafkaInformation) : HealthIndicator {

    override fun health(): Health {
        val nodes = kafkaInformation.getActiveNodesCount()
        return if (nodes < 2) {
            Health.down().withDetail("nodes", nodes).build()
        } else {
            Health.up().withDetail("nodes", nodes).build()
        }
    }
}