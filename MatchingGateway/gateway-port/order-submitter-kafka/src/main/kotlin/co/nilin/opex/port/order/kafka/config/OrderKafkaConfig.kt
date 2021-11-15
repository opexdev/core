package co.nilin.opex.port.order.kafka.config

import co.nilin.opex.matching.core.eventh.events.CoreEvent
import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest
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
    fun producerConfigs(): Map<String, Any>? {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean("orderProducerFactory")
    fun producerFactory(@Qualifier("orderProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, OrderSubmitRequest> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("orderKafkaTemplate")
    fun kafkaTemplate(@Qualifier("orderProducerFactory") producerFactory: ProducerFactory<String?, OrderSubmitRequest>): KafkaTemplate<String?, OrderSubmitRequest> {
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