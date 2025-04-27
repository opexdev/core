package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.inout.KycLevelUpdatedEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.KycLevelUpdatedEventListener
import co.nilin.opex.accountant.ports.postgres.impl.UserLevelLoaderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KycLevelUpdatedListener(val userLevelLoaderImpl: UserLevelLoaderImpl) : KycLevelUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(
        event: KycLevelUpdatedEvent,
        partition: Int, offset: Long, timestamp: Long, eventId: String
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming UserLevelUpdated event: $event")
        logger.info("==========================================================================")
        scope.launch {
            userLevelLoaderImpl.update(event.userId, event.kycLevel)
        }
    }


}