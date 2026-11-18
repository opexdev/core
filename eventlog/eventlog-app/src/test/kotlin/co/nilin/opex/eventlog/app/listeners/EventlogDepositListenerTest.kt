package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class EventlogDepositListenerTest {

    @Test
    fun `persists rich deposit lifecycle event`() {
        val writer = DepositRecordingWriter()
        val listener = EventlogDepositListener(writer)
        val createdAt = LocalDateTime.parse("2026-11-17T12:00:00")
        val transitionedAt = createdAt.plusMinutes(2)

        listener.onEvent(
            DepositEvent(
                uuid = "user-1",
                depositRef = "bank-ref-1",
                depositUuid = "deposit-uuid-1",
                lifecycleEventId = "deposit-lifecycle:deposit-uuid-1:DONE",
                walletTransactionRef = "tx-777",
                businessReference = "bank-ref-1",
                currency = "USDT",
                amount = BigDecimal("25.50"),
                principalAmount = BigDecimal("25.50"),
                appliedCustomerFee = null,
                feeAsset = null,
                transferCategory = "DEPOSIT",
                status = "DONE",
                createDate = createdAt,
                lifecycleTransitionDate = transitionedAt,
                completionDate = transitionedAt
            ),
            partition = 0,
            offset = 10,
            timestamp = 0
        )

        val saved = writer.saved.single()
        assertThat(saved.eventId).isEqualTo("deposit-lifecycle:deposit-uuid-1:DONE")
        assertThat(saved.recordRole).isEqualTo("DEPOSIT_LIFECYCLE")
        assertThat(saved.operationId).isEqualTo("deposit-uuid-1")
        assertThat(saved.walletTransactionRef).isEqualTo("tx-777")
        assertThat(saved.side).isEqualTo("IN")
        assertThat(saved.occurredAt).isEqualTo(transitionedAt)
    }
}

private class DepositRecordingWriter : AccountingTransactionLogWriter {
    val saved = mutableListOf<AccountingTransactionEvent>()

    override suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent {
        saved.add(transaction)
        return transaction
    }

    override suspend fun saveAll(transactions: List<AccountingTransactionEvent>): List<AccountingTransactionEvent> {
        saved.addAll(transactions)
        return transactions
    }
}
