package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import co.nilin.opex.eventlog.ports.kafka.listener.spi.WithdrawRequestListener
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class EventlogWithdrawRequestListener(
    private val transactionLogWriter: AccountingTransactionLogWriter
) : WithdrawRequestListener {
    private val log = LoggerFactory.getLogger(EventlogWithdrawRequestListener::class.java)

    override fun id(): String = "WithdrawRequestListener"

    override fun onEvent(event: WithdrawRequestEvent, partition: Int, offset: Long, timestamp: Long) {
        val operationId = event.withdrawUuid ?: event.withdrawId?.toString()
        val lifecycleEventId = event.lifecycleEventId
            ?: "withdraw-lifecycle:${operationId ?: "unknown"}:${event.status.uppercase()}"
        runBlocking {
            transactionLogWriter.save(
                AccountingTransactionEvent(
                    txId = lifecycleEventId,
                    eventId = lifecycleEventId,
                    userId = event.uuid,
                    type = AccountingTransactionType.WITHDRAW,
                    symbol = null,
                    asset = event.currency,
                    amount = event.amount,
                    fee = event.appliedCustomerFee,
                    feeAsset = event.feeAsset,
                    principalAmount = event.principalAmount,
                    grossAmount = event.amount,
                    withdrawStatus = event.status.uppercase(),
                    transferCategory = event.transferCategory,
                    operationId = operationId,
                    businessReference = event.businessReference,
                    walletTransactionRef = event.walletTransactionRef,
                    recordRole = "WITHDRAW_LIFECYCLE",
                    senderPartyId = event.uuid,
                    perspectivePartyId = event.uuid,
                    counterpartyReferenceId = event.counterpartyReferenceId,
                    sourceAsset = event.currency,
                    sourceAmount = event.principalAmount,
                    source = "CORE_WALLET_WITHDRAW",
                    referenceId = operationId,
                    side = if (event.status.uppercase() == "DONE") "OUT" else null,
                    price = null,
                    quantity = null,
                    operationCreatedAt = event.createDate,
                    lifecycleTransitionAt = event.lifecycleTransitionDate,
                    completedAt = event.completionDate,
                    occurredAt = event.lifecycleTransitionDate ?: event.completionDate ?: event.createDate
                )
            )
        }
        log.info("Persisted withdraw lifecycle event from wallet kafka lifecycleEventId={}", lifecycleEventId)
    }
}
