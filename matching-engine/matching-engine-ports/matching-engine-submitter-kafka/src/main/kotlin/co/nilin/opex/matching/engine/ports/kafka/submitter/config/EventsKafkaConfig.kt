package co.nilin.opex.matching.engine.ports.kafka.submitter.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier

@Configuration
class EventsKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.app.symbols}")
    private val symbols: String? = null

    @Autowired
    private val applicationContext: GenericApplicationContext? = null

    @Bean("eventsProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
    }

    @Bean("eventsProducerFactory")
    fun producerFactory(@Qualifier("eventsProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("eventsKafkaTemplate")
    fun kafkaTemplate(@Qualifier("eventsProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun admin(): KafkaAdmin? {
        val configs: MutableMap<String, Any> = HashMap()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        return KafkaAdmin(configs)
    }

    @Autowired
    fun createTopics() {
        symbols!!.split(",")
            .map { s -> "events_$s" }
            .forEach { topic ->
                applicationContext?.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }
        symbols.split(",")
            .map { s -> "trades_$s" }
            .forEach { topic ->
                applicationContext?.registerBean("topic_${topic}", NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }
    }
}