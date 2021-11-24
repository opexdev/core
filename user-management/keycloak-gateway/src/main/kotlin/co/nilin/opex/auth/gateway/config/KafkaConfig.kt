package co.nilin.opex.auth.gateway.config

import co.nilin.opex.auth.gateway.model.AuthEvent
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class KafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean("authProducerConfigs")
    fun producerConfigs(): Map<String, Any>? {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
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
        applicationContext.registerBean("topic_auth_user_created", NewTopic::class.java, "auth_user_created", 1, 1)
    }
}