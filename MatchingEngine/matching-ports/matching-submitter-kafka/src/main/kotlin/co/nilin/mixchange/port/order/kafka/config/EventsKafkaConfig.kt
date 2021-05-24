package co.nilin.mixchange.port.order.kafka.config


import co.nilin.mixchange.matching.core.eventh.events.CoreEvent
import org.apache.kafka.clients.admin.AdminClientConfig
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
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.StringUtils
import java.util.*


@Configuration
class EventsKafkaConfig() {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.app.symbols}")
    private val symbols: String? = null

    @Autowired
    private val applicationContext: GenericApplicationContext? = null

    @Bean("eventsProducerConfigs")
    fun producerConfigs(): Map<String, Any>? {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
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
    fun createTopics(){
        symbols!!.split(",")
                .map { s -> "events_$s" }
                .map { topic ->
                    applicationContext?.registerBean("topic_${topic}", NewTopic::class.java, topic, 10, 1)
                }
        symbols.split(",")
                .map { s -> "trades_$s" }
                .map { topic ->
                    applicationContext?.registerBean("topic_${topic}", NewTopic::class.java, topic, 10, 1)
                }
    }

}