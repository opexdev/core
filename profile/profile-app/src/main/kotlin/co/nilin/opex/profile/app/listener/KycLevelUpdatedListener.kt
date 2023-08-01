package co.nilin.opex.profile.app.listener

import co.nilin.opex.kyc.core.data.event.KycLevelUpdatedEvent
import co.nilin.opex.profile.app.service.ProfileManagement
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.spi.KycLevelUpdatedEventListener

@Component
class KycLevelUpdatedListener(val userRegistrationService: ProfileManagement) : KycLevelUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)

    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override  fun onEvent(event: co.nilin.opex.core.event.KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String) {
        userRegistrationService.updateUserLevel(event.userId, event.kycLevel)
    }


}