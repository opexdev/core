package co.nilin.opex.eventlog.ports.kafka.listener.spi

import org.apache.kafka.common.header.Headers

interface DLTListener {
    fun id(): String
    fun onEvent(event: String?, partition: Int, offset: Long, timestamp: Long, headers: Headers)
}