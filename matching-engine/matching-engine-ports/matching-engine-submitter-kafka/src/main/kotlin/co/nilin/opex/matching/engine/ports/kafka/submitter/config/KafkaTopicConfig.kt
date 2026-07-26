package co.nilin.opex.matching.engine.ports.kafka.submitter.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import java.util.function.Supplier

@Configuration
class KafkaTopicConfig {

    @Autowired
    private lateinit var symbols: List<String>

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        symbols.map { s -> "orders_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }

        symbols.map { s -> "events_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }

        symbols.map { s -> "trades_$s" }
            .forEach { topic ->
                applicationContext.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }

        applicationContext.registerBean("topic_orderBookUpdate", NewTopic::class.java, Supplier {
            TopicBuilder.name("orderBookUpdate")
                .partitions(10)
                .replicas(1)
                .build()
        })
    }

}
