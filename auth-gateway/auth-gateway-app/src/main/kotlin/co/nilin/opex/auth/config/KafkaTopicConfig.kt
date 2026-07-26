package co.nilin.opex.auth.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import java.util.function.Supplier
@Configuration
class KafkaTopicConfig {
    private val logger = LoggerFactory.getLogger(KafkaTopicConfig::class.java)

    @Value("\${spring.kafka.replica:3}")
    private var replicaCount: Int = 3

    @Value("\${spring.kafka.partitions:10}")
    private var partitionCount: Int = 10

    @Value("\${spring.kafka.min-sync-replica:2}")
    private lateinit var minSyncReplicaCount: String

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        logger.info("Creating kafka topic beans...")

        with(applicationContext) {
            registerBean("topic_auth", NewTopic::class.java, Supplier {
                TopicBuilder.name(KafkaTopics.AUTH)
                    .partitions(partitionCount)
                    .replicas(replicaCount)
                    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minSyncReplicaCount)
                    .build()
            })

            registerBean("topic_login", NewTopic::class.java, Supplier {
                TopicBuilder.name(KafkaTopics.LOGIN)
                    .partitions(partitionCount)
                    .replicas(replicaCount)
                    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minSyncReplicaCount)
                    .build()
            })
            registerBean("topic_logout", NewTopic::class.java, Supplier {
                TopicBuilder.name(KafkaTopics.LOGOUT)
                    .partitions(partitionCount)
                    .replicas(replicaCount)
                    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minSyncReplicaCount)
                    .build()
            })


            logger.info("Kafka topics created")
        }

    }
}