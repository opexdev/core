package co.nilin.opex.wallet.app.config

import co.nilin.opex.wallet.app.service.UserRegistrationService
import co.nilin.opex.wallet.ports.kafka.listener.consumer.AdminEventKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.consumer.UserCreatedKafkaListener
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.AdminEventListener
import co.nilin.opex.wallet.ports.kafka.listener.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Configuration
class AppConfig {

    @Value("\${app.gift.symbol}")
    val symbol: String? = null

    @Value("\${app.gift.amount}")
    val amount: BigDecimal? = null

    @Autowired
    fun configureEventListeners(
        useCreatedKafkaListener: UserCreatedKafkaListener,
        userCreatedEventListener: UserCreatedEventListener,
        adminKafkaEventListener: AdminEventKafkaListener,
        adminEventListener: AdminEventListener,
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
        adminKafkaEventListener.addEventListener(adminEventListener)
    }


}