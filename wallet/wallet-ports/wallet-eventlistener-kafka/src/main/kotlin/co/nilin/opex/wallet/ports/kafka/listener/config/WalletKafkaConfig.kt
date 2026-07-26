package co.nilin.opex.wallet.ports.kafka.listener.config

import co.nilin.opex.wallet.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.FinancialActionKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.ProfileUpdatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.ProfileUpdatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern

@Configuration
@Profile("!otc")
class WalletKafkaConfig(private val environment: Environment) {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    private val logger = LoggerFactory.getLogger(WalletKafkaConfig::class.java)

    @Bean("consumerConfigs")
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "userCreatedEvent:co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent,admin_add_currency:co.nilin.opex.wallet.ports.kafka.listener.model.AddCurrencyEvent,admin_edit_currency:co.nilin.opex.wallet.ports.kafka.listener.model.EditCurrencyEvent,admin_delete_currency:co.nilin.opex.wallet.ports.kafka.listener.model.DeleteCurrencyEvent,financial_action:co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent,profile_updated_event:co.nilin.opex.wallet.ports.kafka.listener.model.ProfileUpdatedEvent"
        )
    }

    @Bean("walletConsumerFactory")
    fun consumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("financialActionConsumerFactory")
    fun financialActionConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, FinancialActionEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean
    fun adminEventsConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String?, AdminEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("profileUpdatedConsumerFactory")
    fun profileUpdatedConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String?, ProfileUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(UserCreatedKafkaListener::class)
    fun configureUserCreatedListener(
        listener: UserCreatedKafkaListener,
        template: KafkaTemplate<String, UserCreatedEvent>,
        @Qualifier("walletConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("auth"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("UserCreatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "auth.DLT")
        container.start()
    }

    @Autowired
    @ConditionalOnBean(FinancialActionKafkaListener::class)
    fun configureFinancialActionListener(
        listener: FinancialActionKafkaListener,
        template: KafkaTemplate<String, FinancialActionEvent>,
        @Qualifier("financialActionConsumerFactory") consumerFactory: ConsumerFactory<String, FinancialActionEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("fiAction"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("FinancialActionKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "fiAction.DLT")
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

    @Autowired
    @ConditionalOnBean(ProfileUpdatedKafkaListener::class)
    fun configureProfileUpdatedListener(
        listener: ProfileUpdatedKafkaListener,
        template: KafkaTemplate<String, ProfileUpdatedEvent>,
        @Qualifier("profileUpdatedConsumerFactory") consumerFactory: ConsumerFactory<String, ProfileUpdatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("profile_updated"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("ProfileUpdatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "profile_updated.DLT")
        container.start()
    }


    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "WALLET".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}