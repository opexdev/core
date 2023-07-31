package co.nilin.opex.profile.ports.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import java.util.function.Supplier

@Configuration
class KafkaTopicConfig {

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_auth_user_created", NewTopic::class.java, Supplier {
            TopicBuilder.name("auth_user_created")
                .partitions(1)
                .replicas(1)
                .build()
        })
    }

}