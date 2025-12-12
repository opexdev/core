package co.nilin.opex.device.ports.kafka.config


import co.nilin.opex.device.core.data.LoginEvent
import co.nilin.opex.device.core.data.LogoutEvent
import co.nilin.opex.device.core.data.SessionEvent
import co.nilin.opex.device.ports.kafka.consumer.LoginKafkaListener
import co.nilin.opex.device.ports.kafka.consumer.LogoutKafkaListener
import org.apache.commons.logging.Log
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff
import java.util.regex.Pattern


@Configuration
class KafkaListenerConfig {
    private val logger = LoggerFactory.getLogger(KafkaListenerConfig::class.java)

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id}")
    private lateinit var groupId: String

    @Bean("consumerConfigs")
    fun consumerConfigs(): Map<String, Any?> {

        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "loginEvent:co.nilin.opex.device.core.data.LoginEvent,logoutEvent:co.nilin.opex.device.core.data.LogoutEvent"

        )
    }


    @Bean("loginConsumerFactory")
    fun loginConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, LoginEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("loginProducerFactory")
    fun loginProducerFactory(@Qualifier("consumerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, LoginEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("loginKafkaTemplate")
    fun loginKafkaTemplate(@Qualifier("loginProducerFactory") producerFactory: ProducerFactory<String, LoginEvent>): KafkaTemplate<String, LoginEvent> {
        return KafkaTemplate(producerFactory)
    }


    @Bean("logoutConsumerFactory")
    fun logoutConsumerFactory(@Qualifier("consumerConfigs") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, LogoutEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("logoutProducerFactory")
    fun logoutProducerFactory(@Qualifier("consumerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, LogoutEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("logoutKafkaTemplate")
    fun logoutKafkaTemplate(@Qualifier("logoutProducerFactory") producerFactory: ProducerFactory<String, LogoutEvent>): KafkaTemplate<String, LogoutEvent> {
        return KafkaTemplate(producerFactory)
    }



    @Bean
    @ConditionalOnBean(LoginKafkaListener::class)
    fun configureLoginListener(
        listener: LoginKafkaListener,
        @Qualifier("loginKafkaTemplate") template: KafkaTemplate<String, LoginEvent>,
        @Qualifier("loginConsumerFactory") consumerFactory: ConsumerFactory<String, LoginEvent>
    ): ConcurrentMessageListenerContainer<String, LoginEvent> {
        val containerProps = ContainerProperties(Pattern.compile("login"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("LoginKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "login.DLT")
        container.start()
        return container
    }

    @Bean
    @ConditionalOnBean(LogoutKafkaListener::class)
    fun configureLogoutListener(
        listener: LogoutKafkaListener,
        @Qualifier("logoutKafkaTemplate") template: KafkaTemplate<String, LogoutEvent>,
        @Qualifier("logoutConsumerFactory") consumerFactory: ConsumerFactory<String, LogoutEvent>
    ): ConcurrentMessageListenerContainer<String, LogoutEvent> {
        val containerProps = ContainerProperties(Pattern.compile("logout"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("logoutKafkaListenerContainer")
        container.commonErrorHandler = createConsumerErrorHandler(template, "logout.DLT")
        container.start()
        return container
    }



    private fun createConsumerErrorHandler(kafkaTemplate: KafkaTemplate<*, *>, dltTopic: String): CommonErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaTemplate) { cr, _ ->
            cr.headers().add("dlt-origin-module", "DEVICE-MANAGEMENT".toByteArray())
            TopicPartition(dltTopic, cr.partition())
        }
        return DefaultErrorHandler(recoverer, FixedBackOff(5_000, 20))
    }

}