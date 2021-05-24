package co.nilin.mixchange.wallet.app.config

import co.nilin.mixchange.auth.gateway.model.UserCreatedEvent
import co.nilin.mixchange.port.wallet.consumer.UserCreatedKafkaListener
import co.nilin.mixchange.port.wallet.spi.UserCreatedEventListener
import co.nilin.mixchange.wallet.app.controller.Symbol
import co.nilin.mixchange.wallet.app.service.UserRegistrationService
import co.nilin.mixchange.wallet.core.model.Amount
import co.nilin.mixchange.wallet.core.spi.WalletManager
import co.nilin.mixchange.wallet.core.spi.WalletOwnerManager
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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