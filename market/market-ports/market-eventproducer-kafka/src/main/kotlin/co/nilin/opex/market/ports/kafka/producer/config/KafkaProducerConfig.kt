package co.nilin.opex.market.ports.kafka.producer.config

import co.nilin.opex.market.core.inout.MarketOrderEvent
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

object KafkaTopics {
    const val MARKET_ORDER = "marketOrder"
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
            JsonSerializer.TYPE_MAPPINGS to "openOrderUpdateEvent:co.nilin.opex.market.ports.kafka.producer.events.OpenOrderUpdateEvent"
        )
    }

    @Bean
    fun producerFactory(producerConfigs: Map<String, Any>): ProducerFactory<String, MarketOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, MarketOrderEvent>): KafkaTemplate<String, MarketOrderEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    fun createUserCreatedTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_marketOrder", NewTopic::class.java, Supplier {
            TopicBuilder.name(KafkaTopics.MARKET_ORDER)
                .partitions(1)
                .replicas(1)
                .build()
        })
    }
}