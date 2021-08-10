package co.nilin.opex.port.accountant.kafka.config

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.matching.core.eventh.events.CoreEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class SubmitterKafkaConfig() {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Autowired
    private val applicationContext: GenericApplicationContext? = null


    @Bean("accountantProducerConfigs")
    fun producerConfigs(): Map<String, Any>? {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
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
    fun richOrderProducerFactory(@Qualifier("accountantProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichOrder> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richOrderKafkaTemplate")
    fun richOrderKafkaTemplate(@Qualifier("richOrderProducerFactory") producerFactory: ProducerFactory<String?, RichOrder>): KafkaTemplate<String?, RichOrder> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_richOrder", NewTopic::class.java, "richOrder", 10, 1)
        applicationContext.registerBean("topic_richTrade", NewTopic::class.java, "richTrade", 10, 1)
    }
}