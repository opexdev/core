package co.nilin.opex.kyc.app.listener

import co.nilin.opex.kyc.core.data.KycRequest
import co.nilin.opex.kyc.core.data.KycStep
import co.nilin.opex.kyc.core.data.NewUserRequest
import co.nilin.opex.kyc.core.data.event.UserCreatedEvent
import co.nilin.opex.kyc.core.spi.UserCreatedEventListener
import co.nilin.opex.kyc.app.service.KycManagement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class UserCreatedListener(val kycManagement: KycManagement) : UserCreatedEventListener {

    private val logger = LoggerFactory.getLogger(UserCreatedListener::class.java)

    val scope= CoroutineScope(Dispatchers.Default)
    override fun id(): String {
        return "UserCreatedEventListener"
    }



    override fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long, id: String) {
        logger.info("==========================================================================")
        logger.info("Incoming UserCreated event: $event")
        logger.info("==========================================================================")
        scope.launch {
            var data = KycRequest().apply {
                userId = event.uuid
                step = KycStep.Register
                processId = id
            }
            kycManagement.kycProcess(data)
        }
    }
}