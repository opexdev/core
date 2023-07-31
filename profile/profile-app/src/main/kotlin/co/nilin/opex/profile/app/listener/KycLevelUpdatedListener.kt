package co.nilin.opex.profile.app.listener

import co.nilin.opex.core.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
@Component
class KycLevelUpdatedListener(val userRegistrationService: ProfileManagement) : KycLevelUpdatedListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)

    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        runBlocking {
            userRegistrationService.registerNewUser(event)
        }
    }

    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String) {

    }
}