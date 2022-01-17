package co.nilin.opex.accountant.ports.kafka.submitter.config

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class SubmitterKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("accountantProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            //ProducerConfig.CLIENT_ID_CONFIG to "", omitting this option as it produces InstanceAlreadyExistsException
        )
    }

    @Bean("accountantEventProducerFactory")
    fun producerFactory(@Qualifier("accountantProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("accountantEventKafkaTemplate")
    fun kafkaTemplate(@Qualifier("accountantEventProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("richTradeProducerFactory")
    fun richTradeProducerFactory(@Qualifier("accountantProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichTrade> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richTradeKafkaTemplate")
    fun richTradeKafkaTemplate(@Qualifier("richTradeProducerFactory") producerFactory: ProducerFactory<String?, RichTrade>): KafkaTemplate<String?, RichTrade> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("richOrderProducerFactory")
    fun richOrderProducerFactory(@Qualifier("accountantProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richOrderKafkaTemplate")
    fun richOrderKafkaTemplate(@Qualifier("richOrderProducerFactory") producerFactory: ProducerFactory<String?, RichOrderEvent>): KafkaTemplate<String?, RichOrderEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        beans {
            bean(name = "topic_richOrder") {
                TopicBuilder.name("richOrder")
                    .partitions(10)
                    .replicas(3)
                    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
            }

            bean("topic_richTrade") {
                TopicBuilder.name("richTrade")
                    .partitions(10)
                    .replicas(3)
                    .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
            }
        }.initialize(applicationContext)
    }
}