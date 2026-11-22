package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.AccountingTransactionListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogAccountingTransactionListener(
    private val transactionLogWriter: AccountingTransactionLogWriter
) : AccountingTransactionListener {

    private val log = LoggerFactory.getLogger(EventlogAccountingTransactionListener::class.java)

    override fun id(): String = "AccountingTransactionListener"

    override fun onEvent(event: AccountingTransactionKafkaEvent, partition: Int, offset: Long, timestamp: Long) {
        runBlocking {
            transactionLogWriter.save(
                AccountingTransactionEvent(
                    txId = event.txId,
                    eventId = event.eventId ?: event.txId,
                    userId = event.userId,
                    type = AccountingTransactionType.valueOf(event.type.uppercase()),
                    symbol = event.symbol,
                    asset = event.asset,
                    amount = event.amount,
                    fee = event.fee,
                    feeAsset = event.feeAsset,
                    principalAmount = event.principalAmount,
                    grossAmount = event.grossAmount,
                    withdrawStatus = event.withdrawStatus,
                    transferCategory = event.transferCategory,
                    operationId = event.operationId,
                    businessReference = event.businessReference,
                    walletTransactionRef = event.walletTransactionRef,
                    recordRole = event.recordRole,
                    senderPartyId = event.senderPartyId,
                    receiverPartyId = event.receiverPartyId,
                    senderWalletId = event.senderWalletId,
                    receiverWalletId = event.receiverWalletId,
                    senderWalletType = event.senderWalletType,
                    receiverWalletType = event.receiverWalletType,
                    perspectivePartyId = event.perspectivePartyId,
                    counterpartyReferenceId = event.counterpartyReferenceId,
                    relatedTransactionId = event.relatedTransactionId,
                    sourceAsset = event.sourceAsset,
                    sourceAmount = event.sourceAmount,
                    destinationAsset = event.destinationAsset,
                    destinationAmount = event.destinationAmount,
                    originalTradeId = event.originalTradeId,
                    baseAsset = event.baseAsset,
                    quoteAsset = event.quoteAsset,
                    quoteAmount = event.quoteAmount,
                    tradeSide = event.tradeSide,
                    participantRole = event.participantRole,
                    orderReference = event.orderReference,
                    counterpartyOrderReference = event.counterpartyOrderReference,
                    financialActionId = event.financialActionId,
                    relatedFinancialActionId = event.relatedFinancialActionId,
                    feeActionId = event.feeActionId,
                    expectedFee = event.expectedFee,
                    expectedFeeAsset = event.expectedFeeAsset,
                    source = event.source,
                    referenceId = event.referenceId,
                    side = event.side,
                    price = event.price,
                    quantity = event.quantity,
                    operationCreatedAt = event.operationCreatedAt,
                    lifecycleTransitionAt = event.lifecycleTransitionAt,
                    completedAt = event.completedAt,
                    occurredAt = event.occurredAt,
            transferMethod = event.transferMethod,
            applicator = event.applicator,
            depositStatus = event.depositStatus,
            operationType = event.operationType
                )
            )
        }
        log.info(
            "Persisted accounting tx from kafka txId={} type={} partition={} offset={}",
            event.txId,
            event.type,
            partition,
            offset
        )
    }
}
