package co.nilin.opex.accountant.ports.kafka.submitter.service

import co.nilin.opex.accountant.core.inout.FinancialActionEvent
import co.nilin.opex.accountant.core.model.FinancialAction
import co.nilin.opex.accountant.core.spi.FinancialActionPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Component
class FinancialActionSubmitter(
    @Qualifier("fiActionKafkaTemplate")
    private val kafkaTemplate: KafkaTemplate<String, FinancialActionEvent>
) : FinancialActionPublisher, EventPublisher {

    private val logger = LoggerFactory.getLogger(FinancialActionSubmitter::class.java)
    override val topic: String = "fiAction"

    override suspend fun publish(fa: FinancialAction): Unit = suspendCoroutine { cont ->
        logger.info("Sending financial action event")
        val sendFuture = kafkaTemplate.send(
            topic,
            with(fa) {
                FinancialActionEvent(
                    uuid,
                    symbol,
                    amount,
                    sender,
                    senderWalletType,
                    receiver,
                    receiverWalletType,
                    createDate,
                    null,
                    eventType + pointer
                )
            }
        )
        sendFuture.addCallback({
            cont.resume(Unit)
        }, {
            logger.error("Error submitting financial action ${fa.uuid}", it)
            cont.resumeWithException(it)
        })
    }
}