package co.nilin.opex.port.order.kafka.service

import co.nilin.opex.port.order.kafka.inout.OrderSubmitRequest
import co.nilin.opex.port.order.kafka.inout.OrderSubmitResult
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class OrderSubmitter(val kafkaTemplate: KafkaTemplate<String, OrderSubmitRequest>) {
    suspend fun submit(order: OrderSubmitRequest): OrderSubmitResult = suspendCoroutine { cont ->
        println("OrderSubmit!")
        val sendFuture = kafkaTemplate.send("orders_${order.pair.leftSideName}_${order.pair.rightSideName}", order)
        sendFuture.addCallback({ sendResult ->
            cont.resume(OrderSubmitResult(sendResult?.recordMetadata?.offset()))
        }, { exception ->
            cont.resumeWithException(exception)
        })
        /*cont.invokeOnCancellation {
            sendFuture.cancel(true)
        }*/
    }



}