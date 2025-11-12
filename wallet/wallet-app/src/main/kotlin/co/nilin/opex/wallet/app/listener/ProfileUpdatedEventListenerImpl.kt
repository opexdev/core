package co.nilin.opex.wallet.app.listener

import co.nilin.opex.wallet.core.spi.WalletOwnerManager
import co.nilin.opex.wallet.ports.kafka.listener.model.ProfileUpdatedEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.ProfileUpdatedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ProfileUpdatedEventListenerImpl(private val walletOwnerManager: WalletOwnerManager) :
    ProfileUpdatedEventListener {

    private val logger = LoggerFactory.getLogger(ProfileUpdatedEventListenerImpl::class.java)

    override fun id() = "ProfileUpdatedEventListener"

    override fun onEvent(event: ProfileUpdatedEvent, partition: Int, offset: Long, timestamp: Long): Unit =
        if (!event.firstName.isNullOrBlank() && !event.lastName.isNullOrBlank()) {
            runBlocking {
                logger.info("Incoming ProfileUpdated event $event")
                walletOwnerManager.updateWalletOwnerName(event.userId, "${event.firstName} ${event.lastName}")

            }
        } else Unit
}
