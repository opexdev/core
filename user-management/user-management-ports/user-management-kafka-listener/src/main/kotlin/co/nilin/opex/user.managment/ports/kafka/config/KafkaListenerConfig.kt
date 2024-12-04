package co.nilin.opex.user.managment.ports.kafka.config


import co.nilin.opex.auth.core.data.KycLevelUpdatedEvent
import co.nilin.opex.user.managment.ports.kafka.consumer.KycLevelUpdatedKafkaListener
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
            JsonDeserializer.TYPE_MAPPINGS to "kyc_level_updated_event:co.nilin.opex.auth.core.data.KycLevelUpdatedEvent"

        )
    }


    @Bean("KycConsumerFactory")
    fun kycConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, KycLevelUpdatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("kycLevelUpdatedProducerFactory")
    fun producerFactory(@Qualifier("consumerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, KycLevelUpdatedEvent> {
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
            cr.headers().add("dlt-origin-module", "AUTH".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}