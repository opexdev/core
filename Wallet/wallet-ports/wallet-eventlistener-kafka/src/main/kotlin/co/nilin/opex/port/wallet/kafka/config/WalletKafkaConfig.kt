package co.nilin.opex.port.wallet.kafka.config

import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.port.wallet.kafka.consumer.UserCreatedKafkaListener
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.regex.Pattern

@Configuration
class WalletKafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Bean("walletConsumerConfig")
    fun consumerConfigs(): Map<String, Any?>? {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.TRUSTED_PACKAGES] = "co.nilin.opex.*"
        return props
    }

    @Bean("walletConsumerFactory")
    fun consumerFactory(@Qualifier("walletConsumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("walletProducerConfig")
    fun producerConfigs(): Map<String, Any?> {
        val props: MutableMap<String, Any?> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean("walletProducerFactory")
    fun producerFactory(@Qualifier("walletProducerConfig") producerConfigs: Map<String, Any?>): ProducerFactory<String?, UserCreatedEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("walletKafkaTemplate")
    fun kafkaTemplate(@Qualifier("walletProducerFactory") producerFactory: ProducerFactory<String?, UserCreatedEvent>): KafkaTemplate<String?, UserCreatedEvent> {
        return KafkaTemplate(producerFactory)
    }


    @Autowired
    @ConditionalOnBean(UserCreatedKafkaListener::class)
    fun configureUserCreatedListener(listener: UserCreatedKafkaListener, @Qualifier("walletConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>) {
        val containerProps = ContainerProperties(Pattern.compile("auth_user_created"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("UserCreatedKafkaListenerContainer")
        container.start()
    }


    @Autowired
    fun createUserCreatedTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_auth_user_created", NewTopic::class.java, "auth_user_created", 1, 1)
    }

}