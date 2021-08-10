package co.nilin.opex.port.wallet.kafka.spi

import co.nilin.opex.auth.gateway.model.UserCreatedEvent

interface UserCreatedEventListener {
    fun id(): String
    fun onEvent(event: UserCreatedEvent, partition: Int, offset: Long, timestamp: Long)
}