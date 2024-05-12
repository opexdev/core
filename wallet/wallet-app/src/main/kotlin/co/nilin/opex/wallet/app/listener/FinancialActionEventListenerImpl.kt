package co.nilin.opex.wallet.app.listener

import co.nilin.opex.utility.error.data.OpexException
import co.nilin.opex.wallet.app.service.TransferService
import co.nilin.opex.wallet.core.inout.FinancialActionResponseEvent
import co.nilin.opex.wallet.core.inout.Status
import co.nilin.opex.wallet.core.spi.FiActionResponseEventSubmitter
import co.nilin.opex.wallet.ports.kafka.listener.model.FinancialActionEvent
import co.nilin.opex.wallet.ports.kafka.listener.spi.FinancialActionEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FinancialActionEventListenerImpl(
    private val transferService: TransferService,
    private val responseSubmitter: FiActionResponseEventSubmitter
) : FinancialActionEventListener {

    private val logger = LoggerFactory.getLogger(FinancialActionEventListenerImpl::class.java)

    override fun id(): String {
        return "FinancialActionEventListener"
    }

    override fun onEvent(event: FinancialActionEvent, partition: Int, offset: Long, timestamp: Long) {
        logger.info("On FinancialActionEvent ${event.uuid}")
        runBlocking(Dispatchers.IO) {
            val responseEvent = FinancialActionResponseEvent(event.uuid, Status.PROCESSED)
            try {
                transferService.transfer(
                    event.symbol,
                    event.senderWalletType,
                    event.sender,
                    event.receiverWalletType,
                    event.receiver,
                    event.amount,
                    event.description,
                    event.transferRef,
                    event.transferCategory,
                    event.additionalData
                )
            } catch (e: OpexException) {
                responseEvent.apply {
                    status = Status.ERROR
                    errorCode = e.error.code()
                    reason = e.error.errorName()
                }
            } catch (e: Exception) {
                responseEvent.apply {
                    status = Status.ERROR
                    reason = e.message
                }
            }

            responseSubmitter.submit(responseEvent)
        }
    }
}