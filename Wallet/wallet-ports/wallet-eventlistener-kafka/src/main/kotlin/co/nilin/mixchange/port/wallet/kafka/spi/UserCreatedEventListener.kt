package co.nilin.mixchange.port.wallet.spi

import co.nilin.mixchange.auth.gateway.model.UserCreatedEvent

interface UserCreatedEventListener {
    fun id(): String
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long)
}