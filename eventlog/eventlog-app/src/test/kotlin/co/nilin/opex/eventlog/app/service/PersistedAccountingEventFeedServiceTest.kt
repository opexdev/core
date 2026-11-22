package co.nilin.opex.eventlog.app.service

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionLogReader
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.core.swap.SwapLogReader
import co.nilin.opex.eventlog.core.swap.SwapRecord
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class PersistedAccountingEventFeedServiceTest {

    @Test
    fun `operation feed does not count settlement or fee facts as extra trade executions`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-18T00:00:00")
        val execution = depositEvent("execution").copy(type = AccountingTransactionType.TRADE,
            recordRole = "TRADE_EXECUTION", occurredAt = from.plusHours(1))
        val settlement = execution.copy(txId = "wallet-trade", eventId = "wallet-trade", recordRole = "TRADE_SETTLEMENT")
        val fee = execution.copy(txId = "wallet-fee", eventId = "wallet-fee",
            type = AccountingTransactionType.FEE, recordRole = "FEE_SETTLEMENT")
        val source = feed(StaticLogReader(listOf(settlement, fee, execution)))
        assertThat(source.findEvents(from, from.plusDays(1), 100)).isEmpty()
    }

    @Test
    fun `raw feed keeps eventlog facts for downstream aggregation`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-18T00:00:00")
        val created = withdrawEvent("created", "CREATED", "WITHDRAW_LIFECYCLE").copy(
            occurredAt = from.plusHours(1),
            lifecycleTransitionAt = from.plusHours(1)
        )
        val fee = created.copy(
            txId = "fee-1",
            eventId = "fee-1",
            type = AccountingTransactionType.FEE,
            recordRole = "FEE_SETTLEMENT"
        )
        val source = feed(StaticLogReader(listOf(created, fee,
            depositEvent("deposit-raw").copy(occurredAt = from.plusHours(2)))))

        val result = source.findRawEvents(from, from.plusDays(1), 100)

        assertThat(result.map { it.eventId ?: it.txId }).contains("withdraw-created", "fee-1", "deposit-raw")
    }

    @Test
    fun `counts completed external withdrawals once and keeps others filtered`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-16T00:00:00")
        val to = from.plusDays(1)
        val reader = StaticLogReader(
            listOf(
                withdrawEvent("created", "CREATED", "WITHDRAW_LIFECYCLE"),
                withdrawEvent("done", "DONE", "WITHDRAW_LIFECYCLE"),
                withdrawEvent("internal", null, "INTERNAL_MOVEMENT"),
                legacyWithdrawEvent("legacy"),
                depositEvent("deposit-1")
            )
        )
        val source = feed(reader)

        val result = source.findEvents(from, to, 100)

        assertThat(result.map { it.eventId ?: it.txId }).contains("withdraw-done", "deposit-1")
        assertThat(result.map { it.eventId ?: it.txId }).doesNotContain("withdraw-created", "withdraw-internal", "withdraw-legacy")
    }

    @Test
    fun `fetches deeper when latest rows are ineligible and limit is small`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-16T00:00:00")
        val to = from.plusDays(1)
        val reader = object : AccountingTransactionLogReader {
            override suspend fun findBetween(
                from: LocalDateTime,
                to: LocalDateTime,
                limit: Int
            ): List<AccountingTransactionEvent> {
                val createdNewer = withdrawEvent("created-newer", "CREATED", "WITHDRAW_LIFECYCLE").copy(
                    occurredAt = LocalDateTime.parse("2026-11-16T10:00:00")
                )
                val doneOlder = withdrawEvent("done-older", "DONE", "WITHDRAW_LIFECYCLE").copy(
                    occurredAt = LocalDateTime.parse("2026-11-16T09:00:00")
                )
                val all = listOf(createdNewer, doneOlder)
                return all.take(limit)
            }
        }
        val source = feed(reader)

        val result = source.findEvents(from, to, 1)

        assertThat(result).hasSize(1)
        assertThat(result.single().eventId).isEqualTo("withdraw-done-older")
    }

    @Test
    fun `keeps manual withdrawal completion records in feed`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-16T00:00:00")
        val to = from.plusDays(1)
        val manualCompletion = withdrawEvent("manual", "DONE", "WITHDRAW_COMPLETION")
        val source = feed(StaticLogReader(listOf(manualCompletion)))

        val result = source.findEvents(from, to, 10)

        assertThat(result).hasSize(1)
        assertThat(result.single().recordRole).isEqualTo("WITHDRAW_COMPLETION")
    }

    @Test
    fun `keeps accepted pending but replaces it with done and suppresses rejected withdrawals`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-16T00:00:00")
        fun state(operation: String, status: String, hour: Long) =
            withdrawEvent("$operation-$status", status, "WITHDRAW_LIFECYCLE").copy(
                operationId = operation, lifecycleTransitionAt = from.plusHours(hour), occurredAt = from.plusHours(hour))
        val pending = state("pending", "ACCEPTED", 8)
        val done = state("completed", "DONE", 11)
        // Intentionally deliver DONE before ACCEPTED; reporting must use state time, not arrival order.
        val data = listOf(done, pending, state("completed", "ACCEPTED", 9), pending,
            state("rejected", "ACCEPTED", 9), state("rejected", "REJECTED", 10),
            state("canceled", "CREATED", 8), state("canceled", "CANCELED", 10))
        val source = feed(StaticLogReader(data))
        val result = source.findEvents(from, from.plusDays(1), 100)
        assertThat(result.map { it.operationId }).containsExactly("completed", "pending")
        assertThat(result.map { it.withdrawStatus }).containsExactly("DONE", "ACCEPTED")
        assertThat(source.findEvents(from, from.plusDays(1), 1).single().operationId).isEqualTo("completed")
    }

    @Test
    fun `historical cutoff does not replace accepted with a future completion`() = runBlocking<Unit> {
        val from = LocalDateTime.parse("2026-11-16T00:00:00")
        val accepted = withdrawEvent("accepted", "ACCEPTED", "WITHDRAW_LIFECYCLE").copy(
            lifecycleTransitionAt = from.plusHours(9), occurredAt = from.plusHours(9))
        val done = accepted.copy(txId = "done", eventId = "done", withdrawStatus = "DONE",
            lifecycleTransitionAt = from.plusHours(12), occurredAt = from.plusHours(12))
        val source = feed(StaticLogReader(listOf(done, accepted)))
        assertThat(source.findEvents(from, from.plusHours(10), 10).single().withdrawStatus).isEqualTo("ACCEPTED")
        assertThat(source.findEvents(from, from.plusHours(13), 10).single().withdrawStatus).isEqualTo("DONE")
    }

    private fun withdrawEvent(suffix: String, status: String?, role: String): AccountingTransactionEvent {
        val t = LocalDateTime.parse("2026-11-16T08:00:00")
        return AccountingTransactionEvent(
            txId = "withdraw-$suffix",
            eventId = "withdraw-$suffix",
            userId = "user-1",
            type = AccountingTransactionType.WITHDRAW,
            symbol = null,
            asset = "USDT",
            amount = BigDecimal("10.10"),
            fee = BigDecimal("0.10"),
            feeAsset = "USDT",
            principalAmount = BigDecimal("10.00"),
            grossAmount = BigDecimal("10.10"),
            withdrawStatus = status,
            transferCategory = "WITHDRAW_ACCEPT",
            operationId = "withdraw-1",
            businessReference = "wallet:withdraw:withdraw-1:DONE",
            walletTransactionRef = "tx-1",
            recordRole = role,
            source = "CORE_WALLET",
            referenceId = "withdraw-1",
            side = "OUT",
            price = null,
            quantity = null,
            operationCreatedAt = t.minusMinutes(5),
            lifecycleTransitionAt = t,
            completedAt = if (status == "DONE") t else null,
            occurredAt = t
        )
    }

    private fun legacyWithdrawEvent(suffix: String): AccountingTransactionEvent {
        val t = LocalDateTime.parse("2026-11-16T08:00:00")
        return AccountingTransactionEvent(
            txId = "withdraw-$suffix",
            eventId = "withdraw-$suffix",
            userId = "user-1",
            type = AccountingTransactionType.WITHDRAW,
            symbol = null,
            asset = "USDT",
            amount = BigDecimal("9.00"),
            fee = BigDecimal.ZERO,
            source = "CORE_WALLET_KAFKA",
            referenceId = "withdraw-legacy",
            side = null,
            price = null,
            quantity = null,
            occurredAt = t
        )
    }

    private fun depositEvent(id: String): AccountingTransactionEvent {
        val t = LocalDateTime.parse("2026-11-16T08:30:00")
        return AccountingTransactionEvent(
            txId = id,
            eventId = id,
            userId = "user-2",
            type = AccountingTransactionType.DEPOSIT,
            symbol = null,
            asset = "USDT",
            amount = BigDecimal("5.00"),
            fee = BigDecimal.ZERO,
            source = "CORE_WALLET",
            referenceId = "dep-1",
            recordRole = "DEPOSIT_LIFECYCLE",
            depositStatus = "DONE",
            side = "IN",
            price = null,
            quantity = null,
            occurredAt = t
        )
    }
}

private class StaticLogReader(private val data: List<AccountingTransactionEvent>) : AccountingTransactionLogReader {
    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int): List<AccountingTransactionEvent> {
        return data.filter { !it.occurredAt.isBefore(from) && !it.occurredAt.isAfter(to) }.take(limit)
    }
}

private fun feed(reader: AccountingTransactionLogReader) = PersistedAccountingEventFeedService(reader, object : RichTradeLogReader {
    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<RichTradeRecord>()
}, object : SwapLogReader {
    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<SwapRecord>()
}, co.nilin.opex.eventlog.app.EmptyFundingReader)
