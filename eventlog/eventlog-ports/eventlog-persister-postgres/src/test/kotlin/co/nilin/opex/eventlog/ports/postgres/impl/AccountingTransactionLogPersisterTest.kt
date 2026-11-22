package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.accounting.AccountingTransactionEvent
import co.nilin.opex.eventlog.core.accounting.AccountingTransactionType
import co.nilin.opex.eventlog.ports.postgres.dao.AccountingTransactionRepository
import co.nilin.opex.eventlog.ports.postgres.model.AccountingTransactionModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class AccountingTransactionLogPersisterTest {

    @Test
    fun `returns persisted event on duplicate with identical payload`() = runBlocking<Unit> {
        val repository = mockk<AccountingTransactionRepository>()
        val persister = AccountingTransactionLogPersister(repository)
        val event = sampleEvent()
        val model = sampleModel(amount = BigDecimal("20.00"))

        coEvery { repository.save(any()) } throws DuplicateKeyException("dup")
        coEvery { repository.findByEventIdAndSource("event-1", "CORE_WALLET") } returns Mono.just(model)
        coEvery { repository.findByTxIdAndSource(any(), any()) } returns Mono.empty()

        val saved = persister.save(event)

        assertThat(saved.eventId).isEqualTo("event-1")
        assertThat(saved.amount).isEqualTo(BigDecimal("20.00"))
    }

    @Test
    fun `throws on duplicate with conflicting payload`() = runBlocking<Unit> {
        val repository = mockk<AccountingTransactionRepository>()
        val persister = AccountingTransactionLogPersister(repository)
        val incoming = sampleEvent()
        val conflicting = sampleModel(amount = BigDecimal("21.00"))

        coEvery { repository.save(any()) } throws DuplicateKeyException("dup")
        coEvery { repository.findByEventIdAndSource("event-1", "CORE_WALLET") } returns Mono.just(conflicting)
        coEvery { repository.findByTxIdAndSource(any(), any()) } returns Mono.empty()

        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(incoming) }
        }
    }

    private fun sampleEvent(): AccountingTransactionEvent {
        val t = LocalDateTime.parse("2026-11-16T14:00:00")
        return AccountingTransactionEvent(
            txId = "event-1",
            eventId = "event-1",
            userId = "user-1",
            type = AccountingTransactionType.WITHDRAW,
            symbol = "USDT",
            asset = "USDT",
            amount = BigDecimal("20.00"),
            fee = BigDecimal("0.10"),
            feeAsset = "USDT",
            principalAmount = BigDecimal("19.90"),
            grossAmount = BigDecimal("20.00"),
            withdrawStatus = "DONE",
            transferCategory = "WITHDRAW_ACCEPT",
            operationId = "withdraw-1",
            businessReference = "wallet:withdraw:withdraw-1:DONE",
            walletTransactionRef = "998",
            recordRole = "WITHDRAW_LIFECYCLE",
            source = "CORE_WALLET",
            referenceId = "withdraw-1",
            side = "OUT",
            price = null,
            quantity = null,
            operationCreatedAt = t.minusMinutes(1),
            lifecycleTransitionAt = t,
            completedAt = t,
            occurredAt = t
        )
    }

    private fun sampleModel(amount: BigDecimal): AccountingTransactionModel {
        val t = LocalDateTime.parse("2026-11-16T14:00:00")
        return AccountingTransactionModel(
            id = 1L,
            txId = "event-1",
            eventId = "event-1",
            userId = "user-1",
            type = "WITHDRAW",
            symbol = "USDT",
            asset = "USDT",
            amount = amount,
            fee = BigDecimal("0.10"),
            feeAsset = "USDT",
            principalAmount = BigDecimal("19.90"),
            grossAmount = amount,
            withdrawStatus = "DONE",
            transferCategory = "WITHDRAW_ACCEPT",
            operationId = "withdraw-1",
            businessReference = "wallet:withdraw:withdraw-1:DONE",
            walletTransactionRef = "998",
            recordRole = "WITHDRAW_LIFECYCLE",
            source = "CORE_WALLET",
            referenceId = "withdraw-1",
            side = "OUT",
            price = null,
            quantity = null,
            operationCreatedAt = t.minusMinutes(1),
            lifecycleTransitionAt = t,
            completedAt = t,
            occurredAt = t,
            createDate = t
        )
    }
}
