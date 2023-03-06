package co.nilin.opex.wallet.ports.kafka.listener.spi

import co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent

interface FinancialActionEventListener {

    fun id(): String

    fun onEvent(event: FinancialActionEvent, partition: Int, offset: Long, timestamp: Long)

}