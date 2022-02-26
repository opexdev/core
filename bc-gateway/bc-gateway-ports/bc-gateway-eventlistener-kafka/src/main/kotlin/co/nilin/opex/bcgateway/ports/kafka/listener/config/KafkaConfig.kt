package co.nilin.opex.bcgateway.ports.kafka.listener.config

import co.nilin.opex.bcgateway.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.bcgateway.ports.kafka.listener.model.AdminEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.util.regex.Pattern

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String? = null

    @Value("\${spring.kafka.consumer.group-id}")
    private val groupId: String? = null

    @Bean
    fun consumerConfigs(): Map<String, Any?> {
        return mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "co.nilin.opex.*",
            JsonDeserializer.TYPE_MAPPINGS to "admin_add_currency:co.nilin.opex.bcgateway.ports.kafka.listener.model.AddCurrencyEvent,admin_edit_currency:co.nilin.opex.bcgateway.ports.kafka.listener.model.EditCurrencyEvent,admin_delete_currency:co.nilin.opex.bcgateway.ports.kafka.listener.model.DeleteCurrencyEvent"
        )
    }

    @Bean
    fun adminEventsConsumerFactory(consumerConfigs: Map<String, Any?>): ConsumerFactory<String?, AdminEvent> {
        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    @Autowired
    @ConditionalOnBean(AdminEventKafkaListener::class)
    fun configureAdminEventListener(
        listener: AdminEventKafkaListener,
        consumerFactory: ConsumerFactory<String?, AdminEvent>
    ) {
        val containerProps = ContainerProperties(Pattern.compile("admin_event"))
        containerProps.messageListener = listener
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProps)
        container.setBeanName("AdminEventKafkaListenerContainer")
        container.start()
    }

}