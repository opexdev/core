package co.nilin.opex.accountant.app.listener

import co.nilin.opex.accountant.ports.kafka.listener.inout.FinancialActionResponseEvent
import co.nilin.opex.accountant.ports.kafka.listener.spi.FAResponseListener

class AccountantFAResponseEventListener : FAResponseListener {

    override fun id(): String {
        return "FAResponseEventListener"
    }

    override fun onEvent(event: FinancialActionResponseEvent, partition: Int, offset: Long, timestamp: Long) {
        TODO("Not yet implemented")
    }
}