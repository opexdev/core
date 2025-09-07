package co.nilin.opex.wallet.ports.kafka.producer.config


import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent
import co.nilin.opex.wallet.ports.kafka.producer.events.*
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier

object KafkaTopics {
    const val WITHDRAW_REQUEST = "withdrawRequest"
}

@Configuration
@Profile("!otc")
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    @Bean("producerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            JsonSerializer.TYPE_MAPPINGS to "withdrawRequestEvent:co.nilin.opex.market.ports.kafka.producer.events.WithdrawRequestEvent,fiAction_response_event:co.nilin.opex.wallet.ports.kafka.submitter.event.FinancialActionResponseEvent"
        )
    }

    @Bean("withdrawRequestProducerFactory")
    fun richTradeProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, WithdrawRequestEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("withdrawRequestKafkaTemplate")
    fun richTradeTemplate(@Qualifier("withdrawRequestProducerFactory") factory: ProducerFactory<String?, WithdrawRequestEvent>): KafkaTemplate<String?, WithdrawRequestEvent> {
        return KafkaTemplate(factory)
    }

    @Bean
    fun userCreatedTemplate(@Qualifier("producerConfigs") configs: Map<String, Any>): KafkaTemplate<String?, UserCreatedEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(configs))
    }

    @Bean
    fun financialActionKafkaTemplate(@Qualifier("producerConfigs") configs: Map<String, Any?>): KafkaTemplate<String?, FinancialActionEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(configs))
    }

    @Bean
    fun adminKafkaTemplate(@Qualifier("producerConfigs") configs: Map<String, Any?>): KafkaTemplate<String?, AdminEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(configs))
    }

    @Bean
    fun kafkaTemplate(@Qualifier("producerConfigs") configs: Map<String, Any?>): KafkaTemplate<String, FinancialActionResponseEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(configs))
    }

    @Autowired
    fun createUserCreatedTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_withdrawRequest", NewTopic::class.java, Supplier {
            TopicBuilder.name(KafkaTopics.WITHDRAW_REQUEST)
                .partitions(1)
                .replicas(1)
                .build()
        })
    }
}