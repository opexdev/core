package co.nilin.opex.profile.app.listener

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.event.UserCreatedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Component
class UserCreatedListener(val userRegistrationService: ProfileManagement) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(UserCreatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "UserCreatedEventListener"
    }


    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        scope.launch {
            userRegistrationService.registerNewUser(event)
        }
    }
}