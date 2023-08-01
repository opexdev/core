package co.nilin.opex.profile.ports.kafka.config


import co.nilin.opex.profile.ports.kafka.consumer.UserCreatedKafkaListener
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
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
@Configuration
class KafkaConfig {
    private val logger = LoggerFactory.getLogger(KafkaConfig::class.java)
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean("consumerConfigs")
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "user_created_event:co.nilin.opex.profile.core.data.event.UserCreatedEvent"

        )
    }



    @Bean("ProfileConsumerFactory")
    fun consumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(UserCreatedKafkaListener::class)
    fun configureUserCreatedListener(
            listener: UserCreatedKafkaListener,
            template: KafkaTemplate<String, UserCreatedEvent>,
            @Qualifier("ProfileConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("auth_user_created"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("UserCreatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "auth_user_created.DLT")
        container.start()
    }



    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "PROFILE".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}