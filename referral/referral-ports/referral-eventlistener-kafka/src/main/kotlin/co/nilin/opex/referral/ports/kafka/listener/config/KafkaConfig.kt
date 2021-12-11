package co.nilin.opex.referral.ports.kafka.listener.config

import co.nilin.opex.matching.engine.core.eventh.events.CoreEvent
import co.nilin.opex.referral.ports.kafka.listener.consumer.RichTradeKafkaListener
import co.nilin.opex.referral.ports.kafka.listener.spi.RichTradeListener
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
class KafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Bean("referralConsumerConfig")
    fun consumerConfigs(): Map<String, Any?>? {
        val props: MutableMap<String, Any?> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[JsonDeserializer.TRUSTED_PACKAGES] = "co.nilin.opex.*"
        return props
    }

    @Bean("referralConsumerFactory")
    fun consumerFactory(@Qualifier("referralConsumerConfig") consumerConfigs: Map<String, Any?>): ConsumerFactory<String, CoreEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Bean("referralProducerConfig")
    fun producerConfigs(): Map<String, Any?> {
        val props: MutableMap<String, Any?> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean("referralProducerFactory")
    fun producerFactory(@Qualifier("referralProducerConfig") producerConfigs: Map<String, Any?>): ProducerFactory<String?, CoreEvent> {
        return DefaultKafkaProducerFactory(producerConfigs)
    }

    @Bean("referralKafkaTemplate")
    fun kafkaTemplate(@Qualifier("referralProducerFactory") producerFactory: ProducerFactory<String?, CoreEvent>): KafkaTemplate<String?, CoreEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Autowired
    @ConditionalOnBean(RichTradeListener::class)
    fun configureTradeListener(
        richTradeListener: RichTradeKafkaListener,
        @Qualifier("referralConsumerFactory") consumerFactory: ConsumerFactory<String, CoreEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("richTrade"))
        containerProps.messageListener = richTradeListener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.beanName = "ReferralRichTradeKafkaListenerContainer"
        container.start()
    }

    @Autowired
    fun createTopics(applicationContext: GenericApplicationContext) {
        applicationContext.registerBean("topic_richTrade", NewTopic::class.java, "richTrade", 10, 1)
    }
}