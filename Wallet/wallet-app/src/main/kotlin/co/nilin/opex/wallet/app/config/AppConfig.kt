package co.nilin.opex.wallet.app.config

import co.nilin.opex.auth.gateway.model.UserCreatedEvent
import co.nilin.opex.port.wallet.kafka.consumer.UserCreatedKafkaListener
import co.nilin.opex.port.wallet.kafka.spi.UserCreatedEventListener
import co.nilin.opex.wallet.app.service.UserRegistrationService
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
    fun configureUserCreatedEventListener(
        useCreatedKafkaListener: UserCreatedKafkaListener,
        userCreatedEventListener: UserCreatedEventListener
    ) {
        useCreatedKafkaListener.addEventListener(userCreatedEventListener)
    }

    @Component
    class WalletUserCreatedEventListener(
        val userRegistrationService: UserRegistrationService
    ) : UserCreatedEventListener {

        override fun id(): String {
            return "UserCreatedEventListener"
        }

        override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long) {
            println("UserCreatedEvent " + event)
            runBlocking(AppDispatchers.kafkaExecutor) {
                userRegistrationService.registerNewUser(event)
            }
            println("onUserCreatedEvent")
        }
    }
}