package co.nilin.opex.auth.kafka

import co.nilin.opex.auth.data.ProfileUpdatedEvent
import co.nilin.opex.auth.proxy.KeycloakProxy
import co.nilin.opex.auth.spi.ProfileUpdatedEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ProfileUpdatedListener(private val keycloakProxy: KeycloakProxy) : ProfileUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(ProfileUpdatedListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "ProfileUpdatedListener"
    }

    override fun onEvent(
        event: ProfileUpdatedEvent, partition: Int, offset: Long, timestamp: Long, eventId: String
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming ProfileUpdatedEvent event: $event")
        logger.info("==========================================================================")
        scope.launch {
            keycloakProxy.updateUserInfo(event.userId, event.mobile, event.email, event.firstName, event.lastName)
        }
    }
}