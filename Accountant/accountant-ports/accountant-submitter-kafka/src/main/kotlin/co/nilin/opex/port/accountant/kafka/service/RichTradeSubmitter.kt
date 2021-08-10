package co.nilin.opex.port.accountant.kafka.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.core.spi.RichTradePublisher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class RichTradeSubmitter(@Qualifier("richTradeKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, RichTrade>) :
    RichTradePublisher {
    override suspend fun publish(trade: RichTrade): Unit = suspendCoroutine { cont ->
        println("richTradeSubmit!")
        val sendFuture = kafkaTemplate.send("richTrade", trade)
        sendFuture.addCallback({ sendResult ->
            cont.resume(Unit)
        }, { exception ->
            cont.resumeWithException(exception)
        })
    }
}