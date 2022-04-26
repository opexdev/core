package co.nilin.opex.wallet.ports.kafka.listener.spi

import co.nilin.opex.wallet.ports.kafka.listener.model.AdminEvent

interface AdminEventListener {

    fun id(): String

    fun onEvent(event: AdminEvent, partition: Int, offset: Long, timestamp: Long)

}