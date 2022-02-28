package co.nilin.opex.matching.engine.ports.kafka.listener.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.engine.core.inout.OrderSubmitRequest
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.matching.engine.ports.kafka.listener.consumer.OrderKafkaListener
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern

@Configuration
class OrderKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Value("\${spring.app.symbols}")
    private val symbols: String? = null

    @Bean("orderConsumerConfigs")
    fun consumerConfigs(): Map<String, Any?>? {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "order_request:co.nilin.opex.matching.engine.core.inout.OrderSubmitRequest"
        )
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
        @Qualifier("orderKafkaTemplate") template: KafkaTemplate<String?, OrderSubmitRequest>,
        @Qualifier("orderConsumerFactory") consumerFactory: ConsumerFactory<String, OrderSubmitRequest>
    ) {
        val topics = symbols!!.split(",").map { s -> "orders_$s" }.toTypedArray()
        val containerProps = ContainerProperties(*topics)
        containerProps.messageListener = orderKafkaListener
        val container = KafkaMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("OrderKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "orders.DLT")
        container.start()
    }

    @Autowired
    fun configureEventListener(
        eventListener: EventKafkaListener,
        @Qualifier("eventsKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("eventConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("events_.*"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("EventKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "events.DLT")
        container.start()
    }

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "MATCHING_ENGINE".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}