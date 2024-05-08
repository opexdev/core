package co.nilin.opex.accountant.ports.kafka.listener.config

import co.nilin.opex.accountant.core.inout.KycLevelUpdatedEvent
import co.nilin.opex.accountant.ports.kafka.listener.consumer.*
import co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent
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
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern

@Configuration
class AccountantKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean("consumerConfig")
    fun consumerConfigs(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "order_request_event:co.nilin.opex.accountant.ports.kafka.listener.inout.OrderRequestEvent,order_request_submit:co.nilin.opex.accountant.ports.kafka.listener.inout.OrderSubmitRequestEvent,order_request_cancel:co.nilin.opex.accountant.ports.kafka.listener.inout.OrderCancelRequestEvent,kyc_level_updated_event:co.nilin.opex.accountant.core.inout.KycLevelUpdatedEvent,fiAction_response_event:co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent"
        )
    }

    @Bean("accountantConsumerFactory")
    fun consumerFactory(@Qualifier("consumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, CoreEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("KycConsumerFactory")
    fun kycConsumerFactory(@Qualifier("consumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, KycLevelUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("faResponseConsumerFactory")
    fun faResponseConsumerFactory(@Qualifier("consumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, FinancialActionResponseEvent> {
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

    @Autowired
    @ConditionalOnBean(FAResponseKafkaListener::class)
    fun configureEventListener(
        eventListener: FAResponseKafkaListener,
        //@Qualifier("accountantEventKafkaTemplate") template: KafkaTemplate<String?, CoreEvent>,
        @Qualifier("faResponseConsumerFactory") consumerFactory: ConsumerFactory<String, FinancialActionResponseEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("fiAction_response"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("FAResponseKafkaListenerContainer")
        //TODO add error handler
        //container.commonErrorHandler = createConsumerErrorHandler(template, "events.DLT")
        container.start()
    }

    @Bean("kycLevelUpdatedProducerFactory")
    fun producerFactory(@Qualifier("consumerConfig") producerConfigs: Map<String, Any>): ProducerFactory<String, KycLevelUpdatedEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("kycLevelUpdatedKafkaTemplate")
    fun kafkaTemplate(@Qualifier("kycLevelUpdatedProducerFactory") producerFactory: ProducerFactory<String, KycLevelUpdatedEvent>): KafkaTemplate<String, KycLevelUpdatedEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    @ConditionalOnBean(KycLevelUpdatedKafkaListener::class)
    fun configureKycLevelUpdatedListener(
        listener: KycLevelUpdatedKafkaListener,
        @Qualifier("kycLevelUpdatedKafkaTemplate") template: KafkaTemplate<String, KycLevelUpdatedEvent>,
        @Qualifier("KycConsumerFactory") consumerFactory: ConsumerFactory<String, KycLevelUpdatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("kyc_level_updated"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("KycLevelUpdatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "kyc_level_updated.DLT")
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