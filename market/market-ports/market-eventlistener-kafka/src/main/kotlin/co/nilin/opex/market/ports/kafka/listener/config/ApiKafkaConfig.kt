package co.nilin.opex.market.ports.kafka.listener.config

import co.nilin.opex.market.core.event.RichOrderEvent
import co.nilin.opex.market.core.event.RichTrade
import co.nilin.opex.market.ports.kafka.listener.consumer.OrderKafkaListener
import co.nilin.opex.market.ports.kafka.listener.consumer.TradeKafkaListener
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
class ApiKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean("apiConsumerConfig")
    fun consumerConfigs(): Map<String, Any> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "rich_order_event:co.nilin.opex.api.core.event.RichOrderEvent,rich_order:co.nilin.opex.api.core.event.RichOrder,rich_order_update:co.nilin.opex.api.core.event.RichOrderUpdate,rich_trade:co.nilin.opex.api.core.event.RichTrade"
        )
    }

    @Bean("richTradeConsumerFactory")
    fun richTradeConsumerFactory(@Qualifier("apiConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, RichTrade> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("richOrderConsumerFactory")
    fun richOrderConsumerFactory(@Qualifier("apiConsumerConfig") consumerConfigs: Map<String, Any>): ConsumerFactory<String, RichOrderEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(TradeKafkaListener::class)
    fun configureTradeListener(
        tradeListener: TradeKafkaListener,
        template: KafkaTemplate<String?, RichTrade>,
        @Qualifier("richTradeConsumerFactory") consumerFactory: ConsumerFactory<String, RichTrade>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("richTrade"))
        containerProps.messageListener = tradeListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("ApiTradeKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "richTrade.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(OrderKafkaListener::class)
    fun configureOrderListener(
        orderListener: OrderKafkaListener,
        template: KafkaTemplate<String?, RichOrderEvent>,
        @Qualifier("richOrderConsumerFactory") consumerFactory: ConsumerFactory<String, RichOrderEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("richOrder"))
        containerProps.messageListener = orderListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("ApiOrderKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "richOrder.DLT")
        container.start()
    }

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "API".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}