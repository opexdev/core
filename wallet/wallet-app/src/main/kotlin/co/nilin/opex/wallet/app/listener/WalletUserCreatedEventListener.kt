package co.nilin.opex.wallet.app.listener

import co.nilin.opex.wallet.app.service.UserRegistrationService
import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WalletUserCreatedEventListener(val userRegistrationService: UserRegistrationService) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(WalletUserCreatedEventListener::class.java)

    override fun id(): String {
        return "UserCreatedEventListener"
    }

    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("Incoming UserCreated event: $event")
        runBlocking {
            userRegistrationService.registerNewUser(event)
        }
        logger.info("onUserCreatedEvent")
    }
}