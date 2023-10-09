package co.nilin.opex.profile.app.listener

import co.nilin.opex.profile.app.service.ProfileManagement
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.spi.KycLevelUpdatedEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Component
class KycLevelUpdatedListener(val userRegistrationService: ProfileManagement) : KycLevelUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(event: co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent,
                         partition: Int, offset: Long, timestamp: Long, eventId: String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserLevelUpdated event: $event")
        logger.info("==========================================================================")
        scope.launch {
            userRegistrationService.updateUserLevel(event.userId, event.kycLevel)
        }
    }


}