package co.nilin.opex.market.ports.kafka.listener.config

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
    fun createTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_richOrder", NewTopic::class.java, Supplier {
            TopicBuilder.name("richOrder")
                .partitions(10)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build()
        })

        applicationContext.registerBean("topic_richTrade", NewTopic::class.java, Supplier {
            TopicBuilder.name("richTrade")
                .partitions(10)
                .replicas(3)
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                .build()
        })
    }

}