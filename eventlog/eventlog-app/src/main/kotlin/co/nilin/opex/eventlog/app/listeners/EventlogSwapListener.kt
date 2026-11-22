package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.core.swap.*
import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.SwapListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogSwapListener(
    private val transactionLogWriter: AccountingTransactionLogWriter,
    private val swapLog: SwapLogWriter
) : SwapListener {
    private val log = LoggerFactory.getLogger(EventlogSwapListener::class.java)
    override fun id(): String = "SwapListener"

    override fun onEvent(event: SwapEvent, partition: Int, offset: Long, timestamp: Long) {
        require(event.txId.isNotBlank() && event.uuid.isNotBlank()) { "Swap requires operation and customer IDs" }
        require(event.quotedRate?.let { it.signum() > 0 } ?: true) { "Quoted rate must be positive" }
        require(event.status == null || event.status == "COMPLETED") { "Only completed swaps are financial executions" }
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
        require(event.sourceAsset.isNotBlank() && event.destAsset.isNotBlank() && event.sourceAsset != event.destAsset) { "Swap requires distinct source and destination assets" }
        require(sourceTransactionId.isNotBlank() && destinationTransactionId.isNotBlank()) { "Swap transaction IDs cannot be blank" }
        require(event.sourceTransactionId != event.destinationTransactionId) { "Swap legs require distinct transaction IDs" }
        require(event.sourceAmount.signum() > 0 && event.destAmount.signum() > 0) { "Swap amounts must be positive" }
        runBlocking {
            swapLog.save(SwapRecord(
                uuid = event.uuid,
                txId = event.txId,
                sourceAsset = event.sourceAsset,
                sourceAmount = event.sourceAmount,
                destAsset = event.destAsset,
                destAmount = event.destAmount,
                sourceTransactionId = sourceTransactionId,
                destinationTransactionId = destinationTransactionId,
                sourceReference = event.sourceReference,
                destinationReference = event.destinationReference,
                systemPartyId = event.systemPartyId,
                receiverPartyId = event.receiverPartyId,
                sourceSenderWalletId = event.sourceSenderWalletId,
                sourceReceiverWalletId = event.sourceReceiverWalletId,
                destinationSenderWalletId = event.destinationSenderWalletId,
                destinationReceiverWalletId = event.destinationReceiverWalletId,
                sourceSenderWalletType = event.sourceSenderWalletType,
                sourceReceiverWalletType = event.sourceReceiverWalletType,
                destinationSenderWalletType = event.destinationSenderWalletType,
                destinationReceiverWalletType = event.destinationReceiverWalletType,
                reservationId = event.reservationId,
                reservationReference = event.reservationReference,
                quotedRate = event.quotedRate,
                quotedAt = event.quotedAt,
                quoteExpiresAt = event.quoteExpiresAt,
                transferCategory = event.transferCategory,
                reservedBy = event.reservedBy,
                executedBy = event.executedBy,
                description = event.description,
                status = event.status,
                createDate = event.createDate
            ), "swap", partition, offset)
        }
    }
}
