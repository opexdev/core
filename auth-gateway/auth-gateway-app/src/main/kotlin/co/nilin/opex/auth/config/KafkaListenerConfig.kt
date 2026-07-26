package co.nilin.opex.auth.config


import co.nilin.opex.auth.data.KycLevelUpdatedEvent
import co.nilin.opex.auth.data.ProfileUpdatedEvent
import co.nilin.opex.auth.kafka.KycLevelUpdatedKafkaListener
import co.nilin.opex.auth.kafka.ProfileUpdatedKafkaListener
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern

@Configuration
class KafkaListenerConfig {
    private val logger = LoggerFactory.getLogger(KafkaListenerConfig::class.java)

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
            JsonDeserializer.TYPE_MAPPINGS to "kyc_level_updated_event:co.nilin.opex.auth.data.KycLevelUpdatedEvent,profile_updated_event:co.nilin.opex.auth.data.ProfileUpdatedEvent"
        )
    }

    // ------------------ KYC Event Beans ------------------

    @Bean("kycLevelKafkaTemplate")
    fun kycKafkaTemplate(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): KafkaTemplate<String, KycLevelUpdatedEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(consumerConfigs))
    }

    @Bean("kycConsumerFactory")
    fun kycConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, KycLevelUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @Bean
    fun kycLevelUpdatedListenerContainer(
        listener: KycLevelUpdatedKafkaListener,
        @Qualifier("kycConsumerFactory") consumerFactory: ConsumerFactory<String, KycLevelUpdatedEvent>,
        @Qualifier("kycLevelKafkaTemplate") template: KafkaTemplate<String, KycLevelUpdatedEvent>
    ): ConcurrentMessageListenerContainer<String, KycLevelUpdatedEvent> {
        val containerProps = ContainerProperties(Pattern.compile("kyc_level_updated"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("KycLevelUpdatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "kyc_level_updated.DLT")
        container.start()
        return container
    }

    // ------------------ Profile Updated Event Beans ------------------

    @Bean("profileUpdatedKafkaTemplate")
    fun profileKafkaTemplate(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): KafkaTemplate<String, ProfileUpdatedEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(consumerConfigs))
    }

    @Bean("profileConsumerFactory")
    fun profileConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, ProfileUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @Bean
    fun profileUpdatedListenerContainer(
        listener: ProfileUpdatedKafkaListener,
        @Qualifier("profileConsumerFactory") consumerFactory: ConsumerFactory<String, ProfileUpdatedEvent>,
        @Qualifier("profileUpdatedKafkaTemplate") template: KafkaTemplate<String, ProfileUpdatedEvent>
    ): ConcurrentMessageListenerContainer<String, ProfileUpdatedEvent> {
        val containerProps = ContainerProperties(Pattern.compile("profile_updated"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("ProfileUpdatedKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "profile_updated.DLT")
        container.start()
        return container
    }

    // ------------------ Shared Error Handler ------------------

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "PROFILE".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}