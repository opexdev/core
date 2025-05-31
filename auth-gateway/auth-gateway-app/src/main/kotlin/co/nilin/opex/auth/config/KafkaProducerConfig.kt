package co.nilin.opex.auth.config

import co.nilin.opex.auth.data.AuthEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier

object KafkaTopics {
    const val AUTH = "auth"
}

@Configuration
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    @Bean
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "userCreatedEvent:co.nilin.opex.auth.data.UserCreatedEvent"
        )
    }

    @Bean
    fun producerFactory(producerConfigs: Map<String, Any>): ProducerFactory<String, AuthEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, AuthEvent>): KafkaTemplate<String, AuthEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    fun createUserCreatedTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_auth", NewTopic::class.java, Supplier {
            TopicBuilder.name(KafkaTopics.AUTH)
                .partitions(1)
                .replicas(1)
                .build()
        })
    }
}