package co.nilin.opex.kyc.ports.kafka.submitter.config


import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.*
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaKycConfig {
    private val logger = LoggerFactory.getLogger(KafkaKycConfig::class.java)
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("producerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ProducerConfig.ACKS_CONFIG to "all",
                JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
                JsonSerializer.TYPE_MAPPINGS to "kyc_level_updated_event:co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent"
                //ProducerConfig.CLIENT_ID_CONFIG to "", omitting this option as it produces InstanceAlreadyExistsException
        )
    }

    @Bean("kycEventProducerFactory")
    fun producerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, KycLevelUpdatedEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("kycEventKafkaTemplate")
    fun kafkaTemplate(@Qualifier("kycEventProducerFactory") producerFactory: ProducerFactory<String?, KycLevelUpdatedEvent>): KafkaTemplate<String?, KycLevelUpdatedEvent> {
        return KafkaTemplate(producerFactory)
    }


}