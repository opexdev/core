package co.nilin.opex.wallet.ports.kafka.listener.config

import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent
import co.nilin.opex.wallet.core.model.AccountingTransactionEvent
import co.nilin.opex.wallet.ports.kafka.listener.model.*
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@Profile("!otc")
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
            JsonSerializer.TYPE_MAPPINGS to "fiAction_response_event:co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent,withdrawRequestEvent:co.nilin.opex.wallet.ports.kafka.listener.model.WithdrawRequestEvent,depositEvent:co.nilin.opex.wallet.ports.kafka.listener.model.DepositEvent,swap_event:co.nilin.opex.wallet.ports.kafka.listener.model.SwapEvent,accounting_transaction_event:co.nilin.opex.wallet.core.model.AccountingTransactionEvent"
        )
    }

    @Bean
    fun userCreatedTemplate(@Qualifier("producerConfigs") configs: Map<String, Any>): KafkaTemplate<String?, UserCreatedEvent> {
        return KafkaTemplate(DefaultKafkaProducerFactory(configs))
    }

    @Bean("profileUpdatedTemplate")
    fun profileUpdatedTemplate(@Qualifier("producerConfigs") configs: Map<String, Any?>): KafkaTemplate<String, ProfileUpdatedEvent> {
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

    @Bean
    fun withdrawRequestKafkaTemplate(@Qualifier("withdrawRequestProducerFactory") factory: ProducerFactory<String?, WithdrawRequestEvent>): KafkaTemplate<String?, WithdrawRequestEvent> {
        return KafkaTemplate(factory)
    }

    @Bean
    fun withdrawRequestProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, WithdrawRequestEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun depositKafkaTemplate(@Qualifier("depositProducerFactory") factory: ProducerFactory<String?, DepositEvent>): KafkaTemplate<String?, DepositEvent> {
        return KafkaTemplate(factory)
    }

    @Bean
    fun depositProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, DepositEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun swapKafkaTemplate(@Qualifier("swapProducerFactory") factory: ProducerFactory<String?, SwapEvent>): KafkaTemplate<String?, SwapEvent> {
        return KafkaTemplate(factory)
    }

    @Bean
    fun swapProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, SwapEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean
    fun accountingTransactionKafkaTemplate(
        @Qualifier("accountingTransactionProducerFactory") factory: ProducerFactory<String?, AccountingTransactionEvent>
    ): KafkaTemplate<String?, AccountingTransactionEvent> {
        return KafkaTemplate(factory)
    }

    @Bean
    fun accountingTransactionProducerFactory(@Qualifier("producerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String?, AccountingTransactionEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

}