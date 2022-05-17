package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.accountant.core.spi.RichTradePublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class RichTradeSubmitter(@Qualifier("richTradeKafkaTemplate") val kafkaTemplate: KafkaTemplate<String, RichTrade>) :
    RichTradePublisher {

    private val logger = LoggerFactory.getLogger(RichTradeSubmitter::class.java)

    override suspend fun publish(trade: RichTrade): Unit = suspendCoroutine { cont ->
        logger.info("Submitting RichTrade event: id=${trade.id}")

        val sendFuture = kafkaTemplate.send("richTrade", trade)
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("RichTrade submitter error", it)
            cont.resume(Unit)
        })
    }
}