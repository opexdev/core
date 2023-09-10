package co.nilin.opex.kyc.ports.kafka.eventlistener.config


import co.nilin.opex.kyc.core.data.event.UserCreatedEvent
import co.nilin.opex.kyc.ports.kafka.eventlistener.consumer.UserCreatedKafkaListener
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
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern


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
                JsonDeserializer.TYPE_MAPPINGS to "user_created_event:co.nilin.opex.kyc.core.data.event.UserCreatedEvent"

        )
    }


    @Bean("KycConsumerFactory")
    fun consumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("userCreatedProducerFactory")
    fun producerFactory(@Qualifier("consumerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, UserCreatedEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("kycKafkaTemplate")
    fun kafkaTemplate(@Qualifier("userCreatedProducerFactory") producerFactory: ProducerFactory<String, UserCreatedEvent>): KafkaTemplate<String, UserCreatedEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    @ConditionalOnBean(UserCreatedKafkaListener::class)
    fun configureUserCreatedListener(
            listener: UserCreatedKafkaListener,
            @Qualifier("kycKafkaTemplate") template: KafkaTemplate<String, UserCreatedEvent>,
            @Qualifier("KycConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>
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
            cr.headers().add("dlt-origin-module", "KYC".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}