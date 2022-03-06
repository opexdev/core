package co.nilin.opex.eventlog.ports.kafka.listener.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
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
class KafkaProducerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("eventLogProducerConfig")
    fun producerConfigs(): Map<String, Any?> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
    }

    @Bean("eventLogProducerFactory")
    fun producerFactory(@Qualifier("eventLogProducerConfig") producerConfigs: Map<String, Any?>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("eventLogKafkaTemplate")
    fun kafkaTemplate(@Qualifier("eventLogProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

}