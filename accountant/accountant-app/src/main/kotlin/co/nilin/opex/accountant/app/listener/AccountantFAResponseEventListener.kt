package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.core.spi.FinancialActionPersister
import co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.FAResponseListener
import kotlinx.coroutines.runBlocking

class AccountantFAResponseEventListener(private val financialActionPersister: FinancialActionPersister) :
    FAResponseListener {

    override fun id(): String {
        return "FAResponseEventListener"
    }

    override fun onEvent(event: FinancialActionResponseEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            financialActionPersister.updateStatus(event.uuid, event.status)
        }
    }
}