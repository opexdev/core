package co.nilin.opex.accountant.ports.kafka.listener.spi

interface Listener<T> {

    fun id(): String

    fun onEvent(event: T, partition: Int, offset: Long, timestamp: Long)

}