package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.inout.DeadLetterEvent
import co.nilin.opex.eventlog.core.spi.DeadLetterPersister
import co.nilin.opex.eventlog.ports.kafka.listener.spi.DLTListener
import kotlinx.coroutines.runBlocking
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class DeadLetterListener(private val persister: DeadLetterPersister) : DLTListener {

    private val logger = LoggerFactory.getLogger(DeadLetterListener::class.java)

    override fun id(): String {
        return "EventLogDeadLetterListener"
    }

    override fun onEvent(event: String, partition: Int, offset: Long, timestamp: Long, headers: Headers) = runBlocking {
        logger.info("Dead letter event received: $event")
        val map = hashMapOf<String, String?>().apply {
            headers.forEach {
                put(it.key(), it.value().toString(Charsets.UTF_8))
            }
        }

        val dlt = DeadLetterEvent(
            map["dlt-origin-module"],
            map[KafkaHeaders.DLT_ORIGINAL_TOPIC],
            map[KafkaHeaders.DLT_ORIGINAL_CONSUMER_GROUP],
            map[KafkaHeaders.DLT_EXCEPTION_MESSAGE],
            map[KafkaHeaders.DLT_EXCEPTION_STACKTRACE],
            map[KafkaHeaders.DLT_EXCEPTION_FQCN],
            event,
            LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId())
        )

        persister.save(dlt)
        logger.info("DLT persisted")
    }


}