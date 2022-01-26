package co.nilin.opex.matching.engine.ports.kafka.listener.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.matching.engine.ports.kafka.listener.inout.OrderSubmitRequest
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier
import java.util.regex.Pattern

@Configuration
class OrderKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Value("\${spring.app.symbols}")
    private val symbols: String? = null

    @Autowired
    private val applicationContext: GenericApplicationContext? = null

    @Autowired
    fun createTopics() {
        symbols!!.split(",")
            .map { s -> "orders_$s" }
            .forEach { topic ->
                applicationContext?.registerBean("topic_${topic}",NewTopic::class.java, Supplier {
                    TopicBuilder.name(topic)
                        .partitions(10)
                        .replicas(3)
                        .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
                        .build()
                })
            }
    }

    @Bean("orderProducerConfigs")
    fun producerConfigs(): Map<String, Any>? {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean("orderProducerFactory")
    fun producerFactory(@Qualifier("orderProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, OrderSubmitRequest> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("orderKafkaTemplate")
    fun kafkaTemplate(@Qualifier("orderProducerFactory") producerFactory: ProducerFactory<String?, OrderSubmitRequest>): KafkaTemplate<String?, OrderSubmitRequest> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("orderConsumerConfigs")
    fun consumerConfigs(): Map<String, Any?>? {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.TRUSTED_PACKAGES] = "co.nilin.opex.*"
        props[JsonDeserializer.TYPE_MAPPINGS] = "order_request:co.nilin.opex.matching.engine.ports.kafka.listener.inout.OrderSubmitRequest"
        return props
    }

    @Bean("orderConsumerFactory")
    fun consumerFactory(@Qualifier("orderConsumerConfigs") consumerConfigs: Map<String, Any>): ConsumerFactory<String, OrderSubmitRequest> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("eventConsumerFactory")
    fun eventConsumerFactory(@Qualifier("orderConsumerConfigs") consumerConfigs: Map<String, Any>): ConsumerFactory<String, CoreEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    fun configureListener(
        orderKafkaListener: OrderKafkaListener,
        @Qualifier("orderConsumerFactory") consumerFactory: ConsumerFactory<String, OrderSubmitRequest>,
        kafkaAdmin: KafkaAdmin
    ) {
        val topics = symbols!!.split(",").map { s -> "orders_$s" }.toTypedArray()
        val containerProps = ContainerProperties(*topics)
        containerProps.messageListener = orderKafkaListener
        val container = KafkaMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("OrderKafkaListenerContainer")
        container.start()
    }

    @Autowired
    fun configureEventListener(
        eventListener: EventKafkaListener,
        @Qualifier("eventConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("events_.*"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("EventKafkaListenerContainer")
        container.start()
    }

}