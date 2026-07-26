package co.nilin.opex.wallet.ports.kafka.listener.spi

import co.nilin.opex.wallet.ports.kafka.listener.model.ProfileUpdatedEvent

interface ProfileUpdatedEventListener {

    fun id(): String

    fun onEvent(event: ProfileUpdatedEvent, partition: Int, offset: Long, timestamp: Long)

}