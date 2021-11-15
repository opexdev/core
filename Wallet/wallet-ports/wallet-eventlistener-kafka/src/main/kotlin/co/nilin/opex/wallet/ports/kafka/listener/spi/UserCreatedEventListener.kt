package co.nilin.opex.wallet.ports.kafka.listener.spi

import co.nilin.opex.wallet.ports.kafka.listener.model.UserCreatedEvent

interface UserCreatedEventListener {
    fun id(): String
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long)
}