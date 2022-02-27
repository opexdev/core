package co.nilin.opex.wallet.ports.kafka.listener.config

import co.nilin.opex.wallet.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
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
class WalletKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "user_created_event:co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent,admin_add_currency:co.nilin.opex.wallet.ports.kafka.listener.model.AddCurrencyEvent,admin_edit_currency:co.nilin.opex.wallet.ports.kafka.listener.model.EditCurrencyEvent,admin_delete_currency:co.nilin.opex.wallet.ports.kafka.listener.model.DeleteCurrencyEvent"
        )
    }

    @Bean("walletConsumerFactory")
    fun consumerFactory(consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean
    fun adminEventsConsumerFactory(consumerConfigs: Map<String, Any?>): ConsumerFactory<String?, AdminEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(UserCreatedKafkaListener::class)
    fun configureUserCreatedListener(
        listener: UserCreatedKafkaListener,
        template: KafkaTemplate<String, UserCreatedEvent>,
        @Qualifier("walletConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("auth_user_created"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("UserCreatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "auth_user_created.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(AdminEventKafkaListener::class)
    fun configureAdminEventListener(
        listener: AdminEventKafkaListener,
        template: KafkaTemplate<String, AdminEvent>,
        consumerFactory: ConsumerFactory<String?, AdminEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("admin_event"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("AdminEventKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "admin_event.DLT")
        container.start()
    }

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}