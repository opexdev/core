package co.nilin.opex.matching.gateway.ports.kafka.submitter.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderRequestEvent
import co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitRequestEvent
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
class OrderKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("orderProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "order_request:co.nilin.opex.matching.gateway.ports.kafka.submitter.inout.OrderSubmitRequest"
        )
    }

    @Bean("orderProducerFactory")
    fun producerFactory(@Qualifier("orderProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, OrderRequestEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("orderKafkaTemplate")
    fun kafkaTemplate(@Qualifier("orderProducerFactory") producerFactory: ProducerFactory<String?, OrderRequestEvent>): KafkaTemplate<String?, OrderRequestEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("gatewayEventProducerFactory")
    fun eventProducerFactory(@Qualifier("orderProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("gatewayEventKafkaTemplate")
    fun eventKafkaTemplate(@Qualifier("gatewayEventProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

}