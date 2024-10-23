package co.nilin.opex.accountant.ports.kafka.submitter.config

import co.nilin.opex.accountant.core.inout.FinancialActionEvent
import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.inout.RichTrade
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
            JsonSerializer.TYPE_MAPPINGS to "rich_order_event:co.nilin.opex.accountant.core.inout.RichOrderEvent,rich_order:co.nilin.opex.accountant.core.inout.RichOrder,rich_order_update:co.nilin.opex.accountant.core.inout.RichOrderUpdate, rich_trade:co.nilin.opex.accountant.core.inout.RichTrade,financial_action:co.nilin.opex.accountant.core.inout.FinancialActionEvent"
            //ProducerConfig.CLIENT_ID_CONFIG to "", omitting this option as it produces InstanceAlreadyExistsException
        )
    }

    @Bean("accountantEventProducerFactory")
    fun producerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("accountantEventKafkaTemplate")
    fun kafkaTemplate(@Qualifier("accountantEventProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("richTradeProducerFactory")
    fun richTradeProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichTrade> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richTradeKafkaTemplate")
    fun richTradeKafkaTemplate(@Qualifier("richTradeProducerFactory") producerFactory: ProducerFactory<String?, RichTrade>): KafkaTemplate<String?, RichTrade> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("richOrderProducerFactory")
    fun richOrderProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, RichOrderEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("richOrderKafkaTemplate")
    fun richOrderKafkaTemplate(@Qualifier("richOrderProducerFactory") producerFactory: ProducerFactory<String?, RichOrderEvent>): KafkaTemplate<String?, RichOrderEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean("fiActionProducerFactory")
    fun fiActionProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, FinancialActionEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("fiActionKafkaTemplate")
    fun fiActionKafkaTemplate(@Qualifier("fiActionProducerFactory") producerFactory: ProducerFactory<String?, FinancialActionEvent>): KafkaTemplate<String?, FinancialActionEvent> {
        return KafkaTemplate(producerFactory)
    }
}