package co.nilin.opex.kyc.app.listener

import co.nilin.opex.kyc.app.service.KYCManagement
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
@Component
class UserCreatedListener(val kycManagement: KYCManagement) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(UserCreatedListener::class.java)

    override fun id(): String {
        return "UserCreatedEventListener"
    }

    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        runBlocking {
            kycManagement.registerNewUser(event)
        }
    }
}