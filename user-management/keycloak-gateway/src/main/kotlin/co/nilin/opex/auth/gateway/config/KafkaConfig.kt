package co.nilin.opex.auth.gateway.config

import co.nilin.opex.auth.core.spi.KycLevelUpdatedEventListener
import co.nilin.opex.auth.gateway.model.AuthEvent
import co.nilin.opex.user.managment.ports.kafka.consumer.KycLevelUpdatedKafkaListener
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.function.Supplier

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("authProducerConfigs")
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ProducerConfig.ACKS_CONFIG to "all",
                JsonSerializer.TYPE_MAPPINGS to "user_created_event:co.nilin.opex.auth.gateway.model.UserCreatedEvent"
        )
    }

    @Bean("authProducerFactory")
    fun producerFactory(@Qualifier("authProducerConfigs") producerConfigs: Map<String, Any>): ProducerFactory<String, AuthEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("authKafkaTemplate")
    fun kafkaTemplate(@Qualifier("authProducerFactory") producerFactory: ProducerFactory<String, AuthEvent>): KafkaTemplate<String, AuthEvent> {
        return KafkaTemplate(producerFactory)
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

    @Autowired
    fun configureEventListeners(

            kycLevelUpdatedKafkaListener: KycLevelUpdatedKafkaListener,
            kycLevelUpdatedEventListener: KycLevelUpdatedEventListener
    ) {
        kycLevelUpdatedKafkaListener.addEventListener(kycLevelUpdatedEventListener)

    }
}