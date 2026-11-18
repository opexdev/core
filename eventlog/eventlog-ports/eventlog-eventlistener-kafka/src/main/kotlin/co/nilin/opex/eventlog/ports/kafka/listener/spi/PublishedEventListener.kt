package co.nilin.opex.eventlog.ports.kafka.listener.spi

interface PublishedEventListener {
    fun id(): String
    fun onEvent(
        topic: String,
        key: String?,
        payload: String,
        partition: Int,
        offset: Long,
        timestamp: Long
    )
}
