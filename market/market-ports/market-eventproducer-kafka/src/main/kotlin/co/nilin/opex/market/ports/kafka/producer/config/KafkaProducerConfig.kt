package co.nilin.opex.market.ports.kafka.producer.config

import co.nilin.opex.market.core.event.RichOrderEvent
import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.core.inout.MarketOrderEvent
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
    const val MARKET_ORDER = "marketOrder"
}

@Configuration
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    @Bean("marketProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "openOrderUpdateEvent:co.nilin.opex.market.ports.kafka.producer.events.OpenOrderUpdateEvent"
        )
    }

    @Bean("richTradeProducerFactory")
    fun richTradeProducerFactory(@Qualifier("marketProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichTrade> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richTradeKafkaTemplate")
    fun richTradeTemplate(@Qualifier("richTradeProducerFactory") factory: ProducerFactory<String?, RichTrade>): KafkaTemplate<String?, RichTrade> {
        return KafkaTemplate(factory)
    }

    @Bean("richOrderProducerFactory")
    fun richOrderProducerFactory(@Qualifier("marketProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richOrderKafkaTemplate")
    fun richOrderTemplate(@Qualifier("richOrderProducerFactory") factory: ProducerFactory<String?, RichOrderEvent>): KafkaTemplate<String?, RichOrderEvent> {
        return KafkaTemplate(factory)
    }

    @Bean("marketOrderKafkaTemplate")
    fun marketOrderProducerFactory(@Qualifier("marketProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, MarketOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun kafkaTemplate(@Qualifier("marketOrderKafkaTemplate") producerFactory: ProducerFactory<String, MarketOrderEvent>): KafkaTemplate<String, MarketOrderEvent> {
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