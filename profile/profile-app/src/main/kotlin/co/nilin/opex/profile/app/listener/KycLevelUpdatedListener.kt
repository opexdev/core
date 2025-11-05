package co.nilin.opex.profile.app.listener

import co.nilin.opex.profile.app.service.ProfileManagement
import co.nilin.opex.profile.core.data.event.KycLevelUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.spi.KycLevelUpdatedEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Component
class KycLevelUpdatedListener(val userRegistrationService: ProfileManagement) : KycLevelUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)
    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(event: KycLevelUpdatedEvent,
                         partition: Int, offset: Long, timestamp: Long, eventId: String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserLevelUpdated event: $event")
        logger.info("==========================================================================")
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                userRegistrationService.updateUserLevel(event.userId, event.kycLevel)
                logger.info("User level updated successfully for ${event.userId}")
            } catch (ex: Exception) {
                logger.error("Failed to update user level for ${event.userId}", ex)
            }
        }
    }


}