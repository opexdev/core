package co.nilin.opex.port.accountant.kafka.service

import co.nilin.opex.accountant.core.inout.RichOrder
import co.nilin.opex.accountant.core.spi.RichOrderPublisher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class RichOrderSubmitter(@Qualifier("richOrderKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, RichOrder>) :
    RichOrderPublisher {
    override suspend fun publish(order: RichOrder): Unit = suspendCoroutine { cont ->
        println("richOrderSubmit!")
        val sendFuture = kafkaTemplate.send("richOrder", order)
        sendFuture.addCallback({ sendResult ->
            cont.resume(Unit)
        }, { exception ->
            cont.resumeWithException(exception)
        })
    }
}