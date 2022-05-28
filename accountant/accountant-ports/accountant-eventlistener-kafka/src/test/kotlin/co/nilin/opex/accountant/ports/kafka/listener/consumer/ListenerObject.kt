package co.nilin.opex.accountant.ports.kafka.listener.consumer

import co.nilin.opex.accountant.ports.kafka.listener.spi.Listener

class ListenerObject : Listener<Any> {

    override fun id(): String {
        return "AnyListener"
    }

    override fun onEvent(event: Any, partition: Int, offset: Long, timestamp: Long) {
        println("event called")
    }
}