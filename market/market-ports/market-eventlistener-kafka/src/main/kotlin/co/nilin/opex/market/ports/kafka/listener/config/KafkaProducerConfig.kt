package co.nilin.opex.market.ports.kafka.listener.config

import co.nilin.opex.market.core.event.RichOrderEvent
import co.nilin.opex.market.core.event.RichTrade
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

    @Bean("marketProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
    }

    @Bean("richTradeProducerFactory")
    fun richTradeProducerFactory(@Qualifier("marketProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichTrade> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richTradeKafkaTemplate")
    fun richTradeTemplate(@Qualifier("richTradeProducerFactory") factory: ProducerFactory<String?, RichTrade>): KafkaTemplate<String?, RichTrade> {
        return KafkaTemplate(factory)
    }

    @Bean("richOrderProducerFactory")
    fun richOrderProducerFactory(@Qualifier("marketProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richOrderKafkaTemplate")
    fun richOrderTemplate(@Qualifier("richOrderProducerFactory") factory: ProducerFactory<String?, RichOrderEvent>): KafkaTemplate<String?, RichOrderEvent> {
        return KafkaTemplate(factory)
    }

}