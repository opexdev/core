package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.DepositListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogDepositListener(
    private val transactionLogWriter: AccountingTransactionLogWriter
) : DepositListener {
    private val log = LoggerFactory.getLogger(EventlogDepositListener::class.java)

    override fun id(): String = "DepositListener"

    override fun onEvent(event: DepositEvent, partition: Int, offset: Long, timestamp: Long) {
        val operationId = event.depositUuid ?: event.depositRef
        val status = event.status?.uppercase()
        val lifecycleEventId = event.lifecycleEventId
            ?: "deposit-lifecycle:${operationId ?: "unknown"}:${status ?: "UNKNOWN"}"
        val occurredAt = event.lifecycleTransitionDate ?: event.completionDate ?: event.createDate ?: java.time.LocalDateTime.now()
        runBlocking {
            transactionLogWriter.save(
                AccountingTransactionEvent(
                    txId = lifecycleEventId,
                    eventId = lifecycleEventId,
                    userId = event.uuid,
                    type = AccountingTransactionType.DEPOSIT,
                    symbol = null,
                    asset = event.currency,
                    amount = event.amount,
                    fee = event.appliedCustomerFee,
                    feeAsset = event.feeAsset,
                    principalAmount = event.principalAmount,
                    grossAmount = event.amount,
                    transferCategory = event.transferCategory,
                    operationId = operationId,
                    businessReference = event.businessReference,
                    walletTransactionRef = event.walletTransactionRef,
                    recordRole = "DEPOSIT_LIFECYCLE",
                    receiverPartyId = event.uuid,
                    perspectivePartyId = event.uuid,
                    counterpartyReferenceId = event.depositRef,
                    destinationAsset = event.currency,
                    destinationAmount = event.amount,
                    source = "CORE_WALLET_DEPOSIT",
                    referenceId = event.depositRef,
                    side = if (status == "DONE") "IN" else null,
                    price = null,
                    quantity = null,
                    operationCreatedAt = event.createDate,
                    lifecycleTransitionAt = event.lifecycleTransitionDate,
                    completedAt = event.completionDate,
                    occurredAt = occurredAt
                )
            )
        }
        log.info("Persisted deposit lifecycle event lifecycleEventId={}", lifecycleEventId)
    }
}
