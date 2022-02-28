package co.nilin.opex.accountant.ports.kafka.listener.config

import co.nilin.opex.accountant.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TempEventKafkaListener
import co.nilin.opex.accountant.ports.kafka.listener.consumer.TradeKafkaListener
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
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
class AccountantKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Bean("accountantConsumerConfig")
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "order_request:co.nilin.opex.accountant.ports.kafka.listener.inout.OrderSubmitRequest"
        )
    }

    @Bean("accountantConsumerFactory")
    fun consumerFactory(@Qualifier("accountantConsumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, CoreEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(TradeKafkaListener::class)
    fun configureTradeListener(
        tradeListener: TradeKafkaListener,
        @Qualifier("accountantEventKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("accountantConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("trades_.*"))
        containerProps.messageListener = tradeListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("TradeKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "trades.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(EventKafkaListener::class)
    fun configureEventListener(
        eventListener: EventKafkaListener,
        @Qualifier("accountantEventKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("accountantConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("events_.*"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("EventKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "events.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(OrderKafkaListener::class)
    fun configureOrderListener(
        orderListener: OrderKafkaListener,
        @Qualifier("accountantEventKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("accountantConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("orders_.*"))
        containerProps.messageListener = orderListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("OrderKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "orders.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(TempEventKafkaListener::class)
    fun configureTempEventListener(
        eventListener: TempEventKafkaListener,
        @Qualifier("accountantEventKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("accountantConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("tempevents"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("TempEventKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "tempevents.DLT")
        container.start()
    }

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "ACCOUNTANT".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}