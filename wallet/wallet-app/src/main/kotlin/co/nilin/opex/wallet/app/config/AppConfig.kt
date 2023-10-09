package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.FinancialActionKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.FinancialActionEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.UserCreatedEventListener
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class AppConfig {

    @Autowired
    fun configureEventListeners(
        useCreatedKafkaListener: UserCreatedKafkaListener,
        userCreatedEventListener: UserCreatedEventListener,
        adminKafkaEventListener: AdminEventKafkaListener,
        adminEventListener: AdminEventListener,
        financialActionKafkaListener: FinancialActionKafkaListener,
        financialActionEventListener: FinancialActionEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
        adminKafkaEventListener.addEventListener(adminEventListener)
        financialActionKafkaListener.addEventListener(financialActionEventListener)
    }

}