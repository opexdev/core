package co.nilin.mixchange.port.accountant.kafka.service

import co.nilin.mixchange.accountant.core.spi.TempEventRepublisher
import co.nilin.mixchange.matching.core.eventh.events.CoreEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class TempEventSubmitter(@Qualifier("tempeventKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, CoreEvent>): TempEventRepublisher {
    override suspend fun republish(events: List<CoreEvent>): Unit = suspendCoroutine { cont ->
        println("TempEventSubmit!")
        events.forEach {  event ->
            val sendFuture = kafkaTemplate.send("tempevents", event)
            sendFuture.addCallback({ sendResult ->
            }, { exception ->
                cont.resumeWithException(exception)
            })
        }
        cont.resume(Unit)
    }




}