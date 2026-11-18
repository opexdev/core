package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.spi.PublishedEventPersister
import co.nilin.opex.eventlog.ports.kafka.listener.spi.PublishedEventListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class EventlogPublishedEventListener(
    private val publishedEventPersister: PublishedEventPersister
) : PublishedEventListener {
    private val log = LoggerFactory.getLogger(EventlogPublishedEventListener::class.java)

    override fun id(): String {
        return "PublishedEventListener"
    }

    override fun onEvent(
        topic: String,
        key: String?,
        payload: String,
        partition: Int,
        offset: Long,
        timestamp: Long
    ) {
        runBlocking {
            publishedEventPersister.save(
                topic = topic,
                partition = partition,
                offset = offset,
                key = key,
                payload = payload,
                eventTimestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
            )
        }
        log.debug("Persisted published event topic={} partition={} offset={}", topic, partition, offset)
    }
}
