package co.nilin.opex.matching.engine.ports.kafka.listener.utils

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashMap


@Component
class KafkaHealthIndicator(private val admin: AdminClient) {

    private val logger = LoggerFactory.getLogger(KafkaHealthIndicator::class.java)

    fun isHealthy(): Boolean {
        return try {
            val clusterInfo = admin.describeCluster()
            if (clusterInfo.nodes().get().size < 2) {
                logger.error("Insufficient kafka node size")
                return false
            }

            return true
        } catch (e: Exception) {
            logger.error("kafka is unhealthy!", e)
            false
        }
    }

}