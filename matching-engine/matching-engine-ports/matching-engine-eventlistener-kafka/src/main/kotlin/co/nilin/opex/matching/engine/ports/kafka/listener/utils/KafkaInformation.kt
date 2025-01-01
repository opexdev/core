package co.nilin.opex.matching.engine.ports.kafka.listener.utils

import org.apache.kafka.clients.admin.AdminClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KafkaInformation(private val admin: AdminClient) {

    private val logger = LoggerFactory.getLogger(KafkaInformation::class.java)

    fun getActiveNodesCount(): Int {
        return try {
            admin.describeCluster().nodes().get().size
        } catch (e: Exception) {
            logger.error("Unable to fetch cluster information")
            -1
        }
    }
}