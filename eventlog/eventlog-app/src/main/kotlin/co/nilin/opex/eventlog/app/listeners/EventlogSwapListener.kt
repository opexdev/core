package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.SwapListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogSwapListener(private val transactionLogWriter: AccountingTransactionLogWriter) : SwapListener {
    private val log = LoggerFactory.getLogger(EventlogSwapListener::class.java)
    override fun id(): String = "SwapListener"

    override fun onEvent(event: SwapEvent, partition: Int, offset: Long, timestamp: Long) {
        val sourceTransactionId = event.sourceTransactionId
        val destinationTransactionId = event.destinationTransactionId
        // Historical per-transfer swap messages cannot establish the complete conversion.
        if (sourceTransactionId == null || destinationTransactionId == null) {
            runBlocking {
                transactionLogWriter.save(AccountingTransactionEvent(
                    txId = event.txId,
                    eventId = "legacy-swap:${event.txId}",
                    userId = event.uuid,
                    type = AccountingTransactionType.SWAP,
                    symbol = "${event.sourceAsset}/${event.destAsset}",
                    asset = event.sourceAsset,
                    amount = event.sourceAmount,
                    fee = null,
                    recordRole = "UNRESOLVED_SWAP",
                    sourceAsset = event.sourceAsset,
                    sourceAmount = event.sourceAmount,
                    destinationAsset = event.destAsset,
                    destinationAmount = event.destAmount,
                    source = "CORE_WALLET_KAFKA",
                    referenceId = event.txId,
                    side = null,
                    price = null,
                    quantity = null,
                    occurredAt = event.createDate
                ))
            }
            log.warn("Stored unresolved legacy swap txId={}; excluded from financial totals until linked", event.txId)
            return
        }
        require(sourceTransactionId.isNotBlank() && destinationTransactionId.isNotBlank()) { "Swap transaction IDs cannot be blank" }
        require(event.sourceTransactionId != event.destinationTransactionId) { "Swap legs require distinct transaction IDs" }
        require(event.sourceAmount.signum() > 0 && event.destAmount.signum() > 0) { "Swap amounts must be positive" }
        val receiver = event.receiverPartyId ?: event.uuid
        val common = AccountingTransactionEvent(
            txId = sourceTransactionId,
            eventId = "swap-leg:${event.txId}:OUT",
            userId = event.uuid,
            type = AccountingTransactionType.SWAP,
            symbol = "${event.sourceAsset}/${event.destAsset}",
            asset = event.sourceAsset,
            amount = event.sourceAmount,
            fee = null,
            operationId = event.txId,
            businessReference = event.sourceReference,
            walletTransactionRef = event.sourceTransactionId,
            relatedTransactionId = event.destinationTransactionId,
            recordRole = "SWAP_LEG",
            senderPartyId = event.uuid,
            receiverPartyId = event.systemPartyId,
            perspectivePartyId = event.uuid,
            senderWalletId = event.sourceSenderWalletId,
            receiverWalletId = event.sourceReceiverWalletId,
            senderWalletType = event.sourceSenderWalletType,
            receiverWalletType = event.sourceReceiverWalletType,
            sourceAsset = event.sourceAsset,
            sourceAmount = event.sourceAmount,
            destinationAsset = event.destAsset,
            destinationAmount = event.destAmount,
            source = "CORE_WALLET_KAFKA",
            referenceId = event.sourceReference,
            side = "OUT",
            price = null,
            quantity = null,
            occurredAt = event.createDate
        )
        runBlocking {
            transactionLogWriter.saveAll(listOf(common, common.copy(
                txId = destinationTransactionId,
                eventId = "swap-leg:${event.txId}:IN",
                userId = receiver,
                asset = event.destAsset,
                amount = event.destAmount,
                businessReference = event.destinationReference,
                walletTransactionRef = event.destinationTransactionId,
                relatedTransactionId = event.sourceTransactionId,
                senderPartyId = event.systemPartyId,
                receiverPartyId = receiver,
                perspectivePartyId = receiver,
                senderWalletId = event.destinationSenderWalletId,
                receiverWalletId = event.destinationReceiverWalletId,
                senderWalletType = event.destinationSenderWalletType,
                receiverWalletType = event.destinationReceiverWalletType,
                referenceId = event.destinationReference,
                side = "IN"
            )))
        }
    }
}
