package co.nilin.opex.admin.ports.kafka.submitter.config

import co.nilin.opex.admin.core.events.AdminEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
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

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "admin_add_currency:co.nilin.opex.admin.core.events.AddCurrencyEvent"
        )
    }

    @Bean
    fun producerFactory(config: Map<String, Any>): ProducerFactory<String?, AdminEvent> {
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(factory: ProducerFactory<String?, AdminEvent>): KafkaTemplate<String?, AdminEvent> {
        return KafkaTemplate(factory)
    }

    @Autowired
    fun createTopic(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_admin_event", NewTopic::class.java, Supplier {
            TopicBuilder.name("admin_event")
                .partitions(1)
                .replicas(1)
                .build()
        })
    }

}