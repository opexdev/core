package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.funding.FundingRecord
import co.nilin.opex.eventlog.ports.postgres.dao.FundingRepository
import co.nilin.opex.eventlog.ports.postgres.model.FundingModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class FundingLogPersisterTest {
    @Test
    fun `full snapshot preserves metadata precision and accepts only identical replay`() = runBlocking<Unit> {
        val repo = mockk<FundingRepository>()
        val persister = FundingLogPersister(repo)
        val saved = slot<FundingModel>()
        every { repo.save(capture(saved)) } answers { Mono.just(saved.captured.copy(id = 1)) }
        val source = FundingRecord("withdraw-lifecycle:w:DONE", "w", "WITHDRAW", "customer", operationDatabaseId = 12,
            currency = "ETH", amount = BigDecimal("0.1234567890123456789"), principalAmount = BigDecimal("0.1234567890123456788"),
            appliedCustomerFee = BigDecimal("0.0000000000000000001"), status = "DONE", transferMethod = "SHEBA",
            operationType = "OFF_CHAIN", applicator = "admin", gatewayId = "gateway", network = "network",
            requestTransactionId = "hold", finalizedTransactionId = "settlement", walletTransactionRef = "settlement",
            destinationAddress = "account", destinationAsset = "ETH", destinationAmount = BigDecimal("0.1234567890123456788"),
            note = "note", attachment = "receipt", statusReason = "reason", otpRequired = 2,
            occurredAt = LocalDateTime.parse("2026-11-18T10:00:00.123456789"))
        val first = persister.save(source, "withdraw_request", 1, 10)
        val model = saved.captured.copy(id = 1)
        assertThat(first).isEqualTo(source.copy(occurredAt = source.occurredAt.withNano(123456000)))
        assertThat(model.sourceOffset).isEqualTo(10)
        every { repo.save(any<FundingModel>()) } returns Mono.error(DuplicateKeyException("duplicate"))
        every { repo.findByTypeAndEventId(source.type, source.eventId) } returns Mono.just(model)
        val replay = persister.save(source.copy(amount = BigDecimal("0.12345678901234567890")), "withdraw_request", 2, 20)
        assertThat(replay).isEqualTo(first)
        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(source.copy(applicator = "other-admin"), "withdraw_request", 2, 21) }
        }
    }
}
