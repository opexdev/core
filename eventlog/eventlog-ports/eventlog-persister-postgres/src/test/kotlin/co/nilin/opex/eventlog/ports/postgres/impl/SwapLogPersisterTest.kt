package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.swap.SwapRecord
import co.nilin.opex.eventlog.ports.postgres.dao.SwapRepository
import co.nilin.opex.eventlog.ports.postgres.model.SwapModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class SwapLogPersisterTest {
    @Test
    fun `preserves all fields and source position and accepts identical operation replay`() = runBlocking<Unit> {
        val repository = mockk<SwapRepository>()
        val persister = SwapLogPersister(repository)
        val captured = slot<SwapModel>()
        every { repository.save(capture(captured)) } answers { Mono.just(firstArg<SwapModel>().copy(id = 1)) }
        val source = sample()
        val saved = persister.save(source, "swap", 2, 10)
        val model = captured.captured.copy(id = 1)
        assertThat(model.sourceTopic).isEqualTo("swap")
        assertThat(model.sourcePartition).isEqualTo(2)
        assertThat(model.sourceOffset).isEqualTo(10)
        assertThat(saved).isEqualTo(source.copy(
            createDate = source.createDate.withNano(123456000),
            quotedAt = source.quotedAt!!.withNano(123456000),
            quoteExpiresAt = source.quoteExpiresAt!!.withNano(123456000)))
        every { repository.save(any<SwapModel>()) } returns Mono.error(DuplicateKeyException("duplicate"))
        every { repository.findByTxId(source.txId) } returns Mono.just(model)
        val replay = persister.save(source.copy(sourceAmount = BigDecimal("10.0000000000000000010")), "swap", 3, 20)
        assertThat(replay).isEqualTo(saved)
        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(source.copy(destinationTransactionId = "103"), "swap", 3, 21) }
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(source.copy(quotedRate = BigDecimal("1")), "swap", 3, 22) }
        }
    }

    @Test
    fun `source-position collision rejects a different operation`() = runBlocking<Unit> {
        val repository = mockk<SwapRepository>()
        val persister = SwapLogPersister(repository)
        every { repository.save(any<SwapModel>()) } answers { Mono.just(firstArg<SwapModel>().copy(id = 1)) }
        persister.save(sample(), "swap", 0, 1)
        val captured = slot<SwapModel>()
        every { repository.save(capture(captured)) } returns Mono.error(DuplicateKeyException("source collision"))
        every { repository.findByTxId("other") } returns Mono.empty()
        // Simulate a row at this source position with a different operation identity.
        every { repository.findBySourceTopicAndSourcePartitionAndSourceOffset("swap", 0, 1) } answers {
            Mono.just(captured.captured.copy(txId = sample().txId))
        }
        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(sample().copy(txId = "other"), "swap", 0, 1) }
        }
    }

    private fun sample(): SwapRecord {
        val time = LocalDateTime.parse("2026-11-18T10:00:00.123456789")
        return SwapRecord(uuid = "customer", txId = "swap:r-1", sourceAsset = "USDT",
            sourceAmount = BigDecimal("10.000000000000000001"), destAsset = "IRT", destAmount = BigDecimal("1500000.01"),
            sourceTransactionId = "101", destinationTransactionId = "102", sourceReference = "r-1-withdraw",
            destinationReference = "r-1-deposit", systemPartyId = "system", receiverPartyId = "recipient",
            sourceSenderWalletId = "11", sourceReceiverWalletId = "21", destinationSenderWalletId = "22",
            destinationReceiverWalletId = "31", sourceSenderWalletType = "MAIN", sourceReceiverWalletType = "MAIN",
            destinationSenderWalletType = "MAIN", destinationReceiverWalletType = "MAIN", reservationId = 7,
            reservationReference = "r-1", quotedRate = BigDecimal("150000.001"), quotedAt = time.minusMinutes(1),
            quoteExpiresAt = time.plusMinutes(4), transferCategory = "IMPERSONATED_PURCHASE_FINALIZED",
            reservedBy = "quote-admin", executedBy = "executor", description = "OTC", status = "COMPLETED", createDate = time)
    }
}
