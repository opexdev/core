package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.spi.UserDepositVolumePersister
import co.nilin.opex.accountant.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.accountant.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.DepositListener
import co.nilin.opex.accountant.ports.kafka.listener.spi.WithdrawRequestListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DepositEventListener(private val userDepositVolumePersister: UserDepositVolumePersister) :
    DepositListener {

    private val logger = LoggerFactory.getLogger(DepositEventListener::class.java)
    val scope = CoroutineScope(Dispatchers.IO)
    override fun id(): String {
        return "DepositEventListener"
    }

    override fun onEvent(
        event: DepositEvent,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        logger.info("==========================================================================")
        logger.info("Incoming Deposit event: $event")
        logger.info("==========================================================================")
        scope.launch {
            userDepositVolumePersister.update(event.uuid, event.currency, event.amount, event.createDate)
        }
    }

}
