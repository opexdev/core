package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.spi.UserWithdrawVolumePersister
import co.nilin.opex.accountant.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.WithdrawRequestListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WithdrawRequestEventListener(private val userWithdrawVolumePersister: UserWithdrawVolumePersister) :
    WithdrawRequestListener {

    private val logger = LoggerFactory.getLogger(WithdrawRequestEventListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "WithdrawRequestEventListener"
    }

    override fun onEvent(
        event: WithdrawRequestEvent,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming WithdrawRequest event: $event")
        logger.info("==========================================================================")
        scope.launch {
            userWithdrawVolumePersister.update(event.uuid, event.currency, event.amount, event.createDate, event.status)
        }
    }

}
