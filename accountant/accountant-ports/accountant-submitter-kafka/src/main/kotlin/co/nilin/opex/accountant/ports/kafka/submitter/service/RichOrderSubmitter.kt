package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.inout.RichOrderEvent
import co.nilin.opex.accountant.core.spi.RichOrderPublisher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class RichOrderSubmitter(@Qualifier("richOrderKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, RichOrderEvent>) :
    RichOrderPublisher {

    override suspend fun publish(order: RichOrderEvent): Unit = suspendCoroutine { cont ->
        println("richOrderSubmit!")
        val sendFuture = kafkaTemplate.send("richOrder", order)
        sendFuture.addCallback({ sendResult ->
            cont.resume(Unit)
        }, { exception ->
            cont.resumeWithException(exception)
        })
    }
}