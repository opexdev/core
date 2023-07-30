package co.nilin.opex.kyc.app.listener

import co.nilin.opex.core.data.KycRequest
import co.nilin.opex.core.data.KycStep
import co.nilin.opex.kyc.app.service.KycManagement
import co.nilin.opex.profile.core.spi.UserCreatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import co.nilin.opex.profile.core.data.profile.UserCreatedEvent
@Component
class UserCreatedListener(val kycManagement: KycManagement) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(UserCreatedListener::class.java)

    override fun id(): String {
        return "UserCreatedEventListener"
    }

    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long) {
    }

    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId:String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        runBlocking {
            kycManagement.kycProcess(KycRequest(userId = event.uuid, step = KycStep.Register, processId = eventId))
        }
    }
}