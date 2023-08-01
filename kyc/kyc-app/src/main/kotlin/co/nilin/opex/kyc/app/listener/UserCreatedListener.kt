package co.nilin.opex.kyc.app.listener

import co.nilin.opex.kyc.core.data.KycRequest
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.NewUserRequest
import co.nilin.opex.kyc.core.data.event.UserCreatedEvent
import co.nilin.opex.kyc.core.spi.UserCreatedEventListener
import co.nilin.opex.kyc.app.service.KycManagement
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserCreatedListener(val kycManagement: KycManagement) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(UserCreatedListener::class.java)

    override fun id(): String {
        return "UserCreatedEventListener"
    }



    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        runBlocking {
            var data = KycRequest().apply {
                userId = event.uuid
                step = KycStep.Register
                processId = eventId
            }
            kycManagement.kycProcess(data)
        }
    }
}