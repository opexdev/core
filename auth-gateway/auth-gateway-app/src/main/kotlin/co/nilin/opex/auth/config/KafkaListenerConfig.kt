package co.nilin.opex.auth.config


import co.nilin.opex.auth.data.KycLevelUpdatedEvent
import co.nilin.opex.auth.kafka.KycLevelUpdatedKafkaListener
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
            JsonDeserializer.TYPE_MAPPINGS to "kyc_level_updated_event:co.nilin.opex.auth.data.KycLevelUpdatedEvent"
        )
    }

    @Bean("kycLevelKafkaTemplate")
    fun kafkaTemplate(consumerConfigs: Map<String, Any?>): KafkaTemplate<String, KycLevelUpdatedEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(consumerConfigs))
    }

    @Bean
    fun kycConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, KycLevelUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @Bean
    fun kycLevelUpdatedListenerContainer(
        listener: KycLevelUpdatedKafkaListener,
        consumerFactory: ConsumerFactory<String, KycLevelUpdatedEvent>,
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

    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "PROFILE".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}