package co.nilin.opex.auth.kafka

import co.nilin.opex.auth.data.KycLevelUpdatedEvent
import co.nilin.opex.auth.data.UserRole
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.spi.KycLevelUpdatedEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class KycLevelUpdatedListener(private val keycloakProxy: KeycloakProxy) : KycLevelUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(KycLevelUpdatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "KycLevelUpdatedListener"
    }

    override fun onEvent(
        event: KycLevelUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming UserLevelUpdated event: $event")
        logger.info("==========================================================================")
        scope.launch {
            keycloakProxy.assignRole(event.userId, UserRole.valueOf(event.kycLevel.name))
        }
    }
}