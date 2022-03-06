package co.nilin.opex.bcgateway.ports.kafka.listener.config

import co.nilin.opex.bcgateway.ports.kafka.listener.model.AdminEvent
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

    @Bean("producerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "admin_add_currency:co.nilin.opex.admin.core.events.AddCurrencyEvent,admin_edit_currency:co.nilin.opex.admin.core.events.EditCurrencyEvent,admin_delete_currency:co.nilin.opex.admin.core.events.DeleteCurrencyEvent"
        )
    }

    @Bean
    fun producerFactory(@Qualifier("producerConfigs") config: Map<String, Any>): ProducerFactory<String?, AdminEvent> {
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(factory: ProducerFactory<String?, AdminEvent>): KafkaTemplate<String?, AdminEvent> {
        return KafkaTemplate(factory)
    }

}