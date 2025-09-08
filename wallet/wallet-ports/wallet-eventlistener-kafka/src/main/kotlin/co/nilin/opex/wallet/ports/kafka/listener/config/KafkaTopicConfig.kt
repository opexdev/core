package co.nilin.opex.wallet.ports.kafka.listener.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import java.util.function.Supplier

@Configuration
@Profile("!otc")
class KafkaTopicConfig {

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_auth", NewTopic::class.java, Supplier {
            TopicBuilder.name("auth")
                .partitions(1)
                .replicas(1)
                .build()
        })
    }

    @Autowired
    fun withdrawRequestTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_withdrawRequest", NewTopic::class.java, Supplier {
            TopicBuilder.name("withdrawRequest")
                .partitions(1)
                .replicas(1)
                .build()
        })
    }

}