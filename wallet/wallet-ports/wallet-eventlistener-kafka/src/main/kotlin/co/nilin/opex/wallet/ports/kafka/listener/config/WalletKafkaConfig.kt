package co.nilin.opex.wallet.ports.kafka.listener.config

import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
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
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier
import java.util.regex.Pattern

@Configuration
class WalletKafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Bean("walletConsumerConfig")
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "user_created_event:co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent"
        )
    }

    @Bean("walletConsumerFactory")
    fun consumerFactory(@Qualifier("walletConsumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, UserCreatedEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("walletProducerConfig")
    fun producerConfigs(): Map<String, Any?> {
        return mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
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
    fun configureUserCreatedListener(
        listener: UserCreatedKafkaListener,
        @Qualifier("walletConsumerFactory") consumerFactory: ConsumerFactory<String, UserCreatedEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("auth_user_created"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.beanName = "UserCreatedKafkaListenerContainer"
        container.start()
    }

    @Autowired
    fun createUserCreatedTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_auth_user_created", NewTopic::class.java, Supplier {
            TopicBuilder.name("auth_user_created")
                .partitions(1)
                .replicas(1)
                .build()
        })
    }

}