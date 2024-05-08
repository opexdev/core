package co.nilin.opex.wallet.ports.kafka.submitter.config

import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class SubmitterKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("producerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "fiAction_response_event:co.nilin.opex.wallet.ports.kafka.submitter.event.FinancialActionResponseEvent"
        )
    }

    @Bean
    fun producerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, FinancialActionResponseEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String?, FinancialActionResponseEvent>): KafkaTemplate<String?, FinancialActionResponseEvent> {
        return KafkaTemplate(producerFactory)
    }
}