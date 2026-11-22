package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.funding.*
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogWriter
import co.nilin.opex.eventlog.ports.kafka.listener.inout.AccountingTransactionKafkaEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class EventlogWithdrawLifecycleListenerTest {

    @Test
    fun `persists created done rejected and canceled lifecycle statuses`() {
        val writer = RecordingWriter()
        val listener = EventlogWithdrawRequestListener(writer)
        val createdAt = LocalDateTime.parse("2026-11-16T10:00:00")

        listOf("CREATED", "ACCEPTED", "DONE", "REJECTED", "CANCELED").forEach { status ->
            listener.onEvent(
                WithdrawRequestEvent(
                    uuid = "user-1",
                    withdrawId = 11L,
                    withdrawUuid = "withdraw-11",
                    lifecycleEventId = "withdraw-lifecycle:withdraw-11:$status",
                    walletTransactionRef = "tx-$status",
                    businessReference = "wallet:withdraw:withdraw-11:$status",
                    currency = "USDT",
                    amount = BigDecimal("101.25"),
                    principalAmount = BigDecimal("100.00"),
                    appliedCustomerFee = BigDecimal("1.25"),
                    feeAsset = "USDT",
                    transferCategory = "WITHDRAW_$status",
                    status = status,
                    createDate = createdAt,
                    lifecycleTransitionDate = createdAt.plusMinutes(5),
                    completionDate = if (status == "DONE") createdAt.plusMinutes(5) else null
                ),
                partition = 1,
                offset = 99L,
                timestamp = System.currentTimeMillis()
            )
        }

        val persistedStatuses = writer.saved.map { it.withdrawStatus }
        assertThat(persistedStatuses).containsExactly("CREATED", "ACCEPTED", "DONE", "REJECTED", "CANCELED")
        assertThat(writer.saved.map { it.principalAmount }).allMatch { it == BigDecimal("100.00") }
        assertThat(writer.saved.map { it.fee }).allMatch { it == BigDecimal("1.25") }
        assertThat(writer.saved.map { it.grossAmount }).allMatch { it == BigDecimal("101.25") }
    }

    @Test
    fun `links dedicated and generic withdrawal events to same operation`() {
        val writer = RecordingWriter()
        val withdrawListener = EventlogWithdrawRequestListener(writer)
        val accountingListener = EventlogAccountingTransactionListener(writer)
        val transitionTime = LocalDateTime.parse("2026-11-16T11:00:00")

        withdrawListener.onEvent(
            WithdrawRequestEvent(
                uuid = "user-1",
                withdrawId = 12L,
                withdrawUuid = "withdraw-12",
                lifecycleEventId = "withdraw-lifecycle:withdraw-12:DONE",
                walletTransactionRef = "tx-12",
                businessReference = "wallet:withdraw:withdraw-12:DONE",
                currency = "USDT",
                amount = BigDecimal("50.10"),
                principalAmount = BigDecimal("50.00"),
                appliedCustomerFee = BigDecimal("0.10"),
                feeAsset = "USDT",
                transferCategory = "WITHDRAW_ACCEPT",
                status = "DONE",
                createDate = transitionTime.minusMinutes(2),
                lifecycleTransitionDate = transitionTime,
                completionDate = transitionTime
            ),
            0,
            0,
            0
        )

        accountingListener.onEvent(
            AccountingTransactionKafkaEvent(
                txId = "wallet-transfer:998",
                eventId = "wallet-transfer:998",
                userId = "user-1",
                type = "WITHDRAW",
                symbol = "USDT",
                asset = "USDT",
                amount = BigDecimal("50.10"),
                fee = BigDecimal.ZERO,
                transferCategory = "WITHDRAW_ACCEPT",
                operationId = "withdraw-12",
                businessReference = "wallet:withdraw:withdraw-12:DONE",
                walletTransactionRef = "998",
                recordRole = "INTERNAL_MOVEMENT",
                source = "CORE_WALLET",
                referenceId = "998",
                side = "OUT",
                occurredAt = transitionTime
            ),
            0,
            1,
            0
        )

        val lifecycle = writer.saved.first { it.recordRole == "WITHDRAW_LIFECYCLE" }
        val internal = writer.saved.first { it.recordRole == "INTERNAL_MOVEMENT" }
        assertThat(lifecycle.operationId).isEqualTo("withdraw-12")
        assertThat(internal.operationId).isEqualTo("withdraw-12")
        assertThat(lifecycle.withdrawStatus).isEqualTo("DONE")
        assertThat(internal.withdrawStatus).isNull()
    }

    @Test
    fun `keeps old withdraw message compatibility`() {
        val writer = RecordingWriter()
        val listener = EventlogWithdrawRequestListener(writer)
        val createDate = LocalDateTime.parse("2026-11-16T12:00:00")

        listener.onEvent(
            WithdrawRequestEvent(
                uuid = "legacy-user",
                withdrawId = 77L,
                currency = "BTC",
                amount = BigDecimal("1.00"),
                status = "DONE",
                createDate = createDate
            ),
            partition = 3,
            offset = 40,
            timestamp = 0
        )

        val saved = writer.saved.single()
        assertThat(saved.eventId).isEqualTo("withdraw-lifecycle:77:DONE")
        assertThat(saved.operationId).isEqualTo("77")
        assertThat(saved.fee).isNull()
        assertThat(saved.lifecycleTransitionAt).isNull()
        assertThat(saved.completedAt).isNull()
        assertThat(saved.operationCreatedAt).isEqualTo(createDate)
        assertThat(saved.occurredAt).isEqualTo(createDate)
    }
}

private class RecordingWriter : AccountingTransactionLogWriter, FundingLogWriter {
    val saved = mutableListOf<AccountingTransactionEvent>()

    override suspend fun save(record: FundingRecord, topic: String, partition: Int, offset: Long): FundingRecord {
        saved.add(record.toAccountingEvent())
        return record
    }

    override suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent {
        saved.add(transaction)
        return transaction
    }

    override suspend fun saveAll(transactions: List<AccountingTransactionEvent>): List<AccountingTransactionEvent> {
        saved.addAll(transactions)
        return transactions
    }
}
