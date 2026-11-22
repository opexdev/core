package co.nilin.opex.eventlog.ports.kafka.listener.config

import co.nilin.opex.eventlog.ports.kafka.listener.consumer.DLTKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.DepositKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.EventKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.AccountingTransactionKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.PublishedEventKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.SwapKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.consumer.WithdrawRequestKafkaListener
import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.util.regex.Pattern

@Configuration
class EventLogKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Value("\${app.eventlog.capture.enabled:true}")
    private var publishedCaptureEnabled: Boolean = true

    @Value("\${app.eventlog.capture.topic-pattern:.*}")
    private lateinit var publishedTopicPattern: String

    @Value("\${app.accounting.kafka.enabled:true}")
    private var accountingKafkaEnabled: Boolean = true

    @Value("\${app.accounting.kafka.transaction-topic-pattern:accounting_transactions}")
    private lateinit var accountingTransactionTopicPattern: String

    @Bean("eventLogConsumerConfig")
    fun consumerConfigs(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "order_request_event:co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderRequestEvent,order_request_submit:co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderSubmitRequestEvent,order_request_cancel:co.nilin.opex.eventlog.ports.kafka.listener.inout.OrderCancelRequestEvent,accounting_transaction_event:co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent,withdrawRequestEvent:co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent,depositEvent:co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent,swap_event:co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent"
        )
    }

    @Bean("dltConsumerConfig")
    fun dltConsumerConfig(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
        )
    }

    @Bean("publishedEventConsumerConfig")
    fun publishedEventConsumerConfig(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "$groupId-published-events",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
    }

    @Bean("eventLogConsumerFactory")
    fun consumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, CoreEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("orderRequestConsumerFactory")
    fun orderRequestConsumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, OrderRequestEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("accountingTransactionConsumerFactory")
    fun accountingTransactionConsumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, AccountingTransactionKafkaEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("withdrawRequestConsumerFactory")
    fun withdrawRequestConsumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, WithdrawRequestEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("depositConsumerFactory")
    fun depositConsumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, DepositEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("swapConsumerFactory")
    fun swapConsumerFactory(@Qualifier("eventLogConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, SwapEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean
    fun dltConsumerFactory(@Qualifier("dltConsumerConfig") configs: Map<String, Any>): ConsumerFactory<String?, String> {
        return DefaultKafkaConsumerFactory(configs)
    }

    @Bean("publishedEventConsumerFactory")
    fun publishedEventConsumerFactory(@Qualifier("publishedEventConsumerConfig") configs: Map<String, Any>): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(configs)
    }

    @Autowired
    @ConditionalOnBean(EventKafkaListener::class)
    fun configureEventListener(
        eventListener: EventKafkaListener,
        @Qualifier("eventLogConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("events_.*"))
        containerProps.messageListener = eventListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("EventKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(OrderKafkaListener::class)
    fun configureOrderListener(
        orderListener: OrderKafkaListener,
        @Qualifier("orderRequestConsumerFactory") consumerFactory: ConsumerFactory<String, OrderRequestEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("orders_.*"))
        containerProps.messageListener = orderListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("OrderKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(OrderKafkaListener::class)
    fun configureDLTListener(
        orderListener: DLTKafkaListener,
        consumerFactory: ConsumerFactory<String?, String>
    ) {
        val containerProps = ContainerProperties(Pattern.compile(".*\\.DLT"))
        containerProps.messageListener = orderListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("DLTKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(PublishedEventKafkaListener::class)
    fun configurePublishedEventListener(
        listener: PublishedEventKafkaListener,
        @Qualifier("publishedEventConsumerFactory") consumerFactory: ConsumerFactory<String, String>
    ) {
        if (!publishedCaptureEnabled) {
            return
        }
        val containerProps = ContainerProperties(Pattern.compile(publishedTopicPattern))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("PublishedEventKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(AccountingTransactionKafkaListener::class)
    fun configureAccountingTransactionListener(
        listener: AccountingTransactionKafkaListener,
        @Qualifier("accountingTransactionConsumerFactory") consumerFactory: ConsumerFactory<String, AccountingTransactionKafkaEvent>
    ) {
        if (!accountingKafkaEnabled) {
            return
        }
        val containerProps = ContainerProperties(Pattern.compile(accountingTransactionTopicPattern))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("AccountingTransactionKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(WithdrawRequestKafkaListener::class)
    fun configureWithdrawRequestListener(
        listener: WithdrawRequestKafkaListener,
        @Qualifier("withdrawRequestConsumerFactory") consumerFactory: ConsumerFactory<String, WithdrawRequestEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("withdraw_request"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("WithdrawRequestKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(DepositKafkaListener::class)
    fun configureDepositListener(
        listener: DepositKafkaListener,
        @Qualifier("depositConsumerFactory") consumerFactory: ConsumerFactory<String, DepositEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("deposit"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("DepositKafkaListenerContainer")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(SwapKafkaListener::class)
    fun configureSwapListener(
        listener: SwapKafkaListener,
        @Qualifier("swapConsumerFactory") consumerFactory: ConsumerFactory<String, SwapEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("swap"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("SwapKafkaListenerContainer")
        container.start()
    }

}