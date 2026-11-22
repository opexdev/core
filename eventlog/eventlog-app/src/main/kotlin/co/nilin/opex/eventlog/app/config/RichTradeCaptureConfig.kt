package co.nilin.opex.eventlog.app.config

import co.nilin.opex.eventlog.ports.kafka.listener.inout.RichTradeEvent
import co.nilin.opex.eventlog.app.listeners.EventlogRichTradeListener
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@ConditionalOnProperty(value = ["app.accounting.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class RichTradeCaptureConfig {
    @Bean
    fun richTradeCaptureContainer(
        @Qualifier("eventLogConsumerConfig") baseConfig: Map<String, Any>,
        listener: EventlogRichTradeListener
    ): ConcurrentMessageListenerContainer<String, RichTradeEvent> {
        val config = baseConfig + mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to (baseConfig[ConsumerConfig.GROUP_ID_CONFIG].toString() + "-richtrade"),
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false,
            JsonDeserializer.VALUE_DEFAULT_TYPE to RichTradeEvent::class.java.name
        )
        val properties = ContainerProperties("richTrade")
        properties.messageListener = listener
        return ConcurrentMessageListenerContainer(DefaultKafkaConsumerFactory<String, RichTradeEvent>(config), properties)
    }
}
