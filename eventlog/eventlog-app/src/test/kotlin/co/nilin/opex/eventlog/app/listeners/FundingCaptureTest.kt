package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.funding.*
import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.app.EmptyFundingReader
import co.nilin.opex.eventlog.app.service.PersistedAccountingEventFeedService
import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.core.swap.*
import co.nilin.opex.eventlog.ports.kafka.listener.inout.DepositEvent
import co.nilin.opex.eventlog.ports.kafka.listener.inout.WithdrawRequestEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class FundingCaptureTest {
    private val time = LocalDateTime.parse("2026-11-18T10:00:00")

    @Test
    fun `producer contracts retain all funding metadata through JSON and capture`() {
        val mapper = jacksonObjectMapper().findAndRegisterModules()
        val rows = mutableListOf<FundingRecord>()
        val writer = object : FundingLogWriter {
            override suspend fun save(record: FundingRecord, topic: String, partition: Int, offset: Long): FundingRecord {
                rows.add(record)
                return record
            }
        }
        val producer = co.nilin.opex.wallet.ports.kafka.listener.model.DepositEvent(
            uuid = "customer", depositRef = "bank-ref", depositUuid = "deposit-1", currency = "ETH",
            amount = BigDecimal("0.1234567890123456789"), principalAmount = BigDecimal("0.1234567890123456789"),
            status = "DONE", transferCategory = "DEPOSIT", transferMethod = "REWARD", operationType = "OFF_CHAIN",
            depositId = 11, gatewayId = "gateway", network = "network", sourceAddress = "funding-address",
            sourceSymbol = "ETH", acceptedFee = BigDecimal("0.00001"), applicator = "admin",
            note = "reward credit", attachment = "attachment-ref", walletTransactionRef = "tx-1",
            senderPartyId = "system", receiverPartyId = "customer", senderWalletId = "1", receiverWalletId = "2",
            senderWalletType = "MAIN", receiverWalletType = "MAIN", createDate = time,
            lifecycleTransitionDate = time.plusSeconds(1), completionDate = time.plusSeconds(1))
        val decoded = mapper.readValue(mapper.writeValueAsBytes(producer), DepositEvent::class.java)
        EventlogDepositListener(writer).onEvent(decoded, 0, 1, 0)
        val deposit = rows.single()
        assertThat(deposit.transferMethod).isEqualTo("REWARD")
        assertThat(deposit.transferCategory).isEqualTo("DEPOSIT")
        assertThat(deposit.operationDatabaseId).isEqualTo(11)
        assertThat(deposit.sourceAddress).isEqualTo("funding-address")
        assertThat(deposit.gatewayId).isEqualTo("gateway")
        assertThat(deposit.attachment).isEqualTo("attachment-ref")
        assertThat(deposit.senderWalletId).isEqualTo("1")
        assertThat(deposit.amount).isEqualByComparingTo("0.1234567890123456789")

        val withdrawalProducer = co.nilin.opex.wallet.ports.kafka.listener.model.WithdrawRequestEvent(
            uuid = "customer", withdrawId = 12, withdrawUuid = "withdraw-1", currency = "USDT",
            amount = BigDecimal("101"), principalAmount = BigDecimal("100"), appliedCustomerFee = BigDecimal.ONE,
            status = co.nilin.opex.wallet.core.model.WithdrawStatus.DONE, createDate = time,
            transferMethod = "SHEBA", operationType = "OFF_CHAIN", applicator = "finalizing-admin",
            gatewayId = "bank-gateway", network = "SHEBA", destinationAddress = "bank-account",
            destinationAsset = "USDT", destinationAmount = BigDecimal("100"), requestTransactionId = "hold-tx",
            finalizedTransactionId = "final-tx", walletTransactionRef = "final-tx", counterpartyReferenceId = "payout-ref",
            operationWalletId = "cashout-wallet", statusReason = "approved", note = "payout note",
            attachment = "receipt-ref", otpRequired = 2, lifecycleTransitionDate = time.plusMinutes(1),
            completionDate = time.plusMinutes(1), senderPartyId = "customer", receiverPartyId = "system")
        val withdrawal = mapper.readValue(mapper.writeValueAsBytes(withdrawalProducer), WithdrawRequestEvent::class.java)
        EventlogWithdrawRequestListener(writer).onEvent(withdrawal, 0, 2, 0)
        val done = rows.last()
        assertThat(done.requestTransactionId).isEqualTo("hold-tx")
        assertThat(done.finalizedTransactionId).isEqualTo("final-tx")
        assertThat(done.counterpartyReferenceId).isEqualTo("payout-ref")
        assertThat(done.applicator).isEqualTo("finalizing-admin")
        assertThat(done.destinationAddress).isEqualTo("bank-account")
        assertThat(done.destinationAmount).isEqualByComparingTo("100")
        assertThat(done.otpRequired).isEqualTo(2)
        fun fields(type: Class<*>) = type.declaredFields.filterNot { it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers) }.map { it.name }
        assertThat(fields(DepositEvent::class.java)).containsExactlyInAnyOrderElementsOf(fields(producer.javaClass))
        assertThat(fields(WithdrawRequestEvent::class.java)).containsExactlyInAnyOrderElementsOf(fields(withdrawalProducer.javaClass))
    }

    @Test
    fun `report uses canonical deposit once while raw feed retains movement and lifecycle`() = runBlocking<Unit> {
        val deposit = FundingRecord("deposit-lifecycle:d:DONE", "d", "DEPOSIT", "customer", currency = "USDT",
            amount = BigDecimal.TEN, status = "DONE", transferCategory = "DEPOSIT", transferMethod = "REWARD",
            occurredAt = time, walletTransactionRef = "wallet-1")
        val funding = object : FundingLogReader {
            override suspend fun findOperation(type: String, operationId: String) = listOf(deposit)
            override suspend fun findReportable(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(deposit)
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(deposit)
        }
        val movement = deposit.toAccountingEvent().copy(txId = "wallet-1", eventId = "wallet-transfer:wallet-1",
            recordRole = "TRANSFER", source = "CORE_WALLET")
        val facts = object : AccountingTransactionLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(movement)
        }
        val trades = object : RichTradeLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<RichTradeRecord>()
        }
        val swaps = object : SwapLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<SwapRecord>()
        }
        val feed = PersistedAccountingEventFeedService(facts, trades, swaps, funding)
        assertThat(feed.findEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(1)
        assertThat(feed.findRawEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(2)
    }

    @Test
    fun `manual lifecycle supersedes its completion movement without dropping legacy manual withdrawals`() = runBlocking<Unit> {
        val done = FundingRecord("withdraw-lifecycle:w:DONE", "w", "WITHDRAW", "customer", currency = "USDT",
            amount = BigDecimal.TEN, status = "DONE", transferCategory = "WITHDRAW_MANUALLY", transferMethod = "MANUALLY",
            applicator = "admin", occurredAt = time, walletTransactionRef = "wallet-1")
        val funding = object : FundingLogReader {
            override suspend fun findOperation(type: String, operationId: String) = listOf(done)
            override suspend fun findReportable(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(done)
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(done)
        }
        val movement = done.toAccountingEvent().copy(txId = "wallet-1", eventId = "wallet-transfer:wallet-1",
            recordRole = "WITHDRAW_COMPLETION", source = "CORE_WALLET")
        val legacy = movement.copy(txId = "legacy", eventId = "legacy", operationId = "legacy-op")
        val facts = object : AccountingTransactionLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(movement, legacy)
            override suspend fun findReportableWithdrawals(from: LocalDateTime, to: LocalDateTime, limit: Int) = listOf(movement, legacy)
        }
        val trades = object : RichTradeLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<RichTradeRecord>()
        }
        val swaps = object : SwapLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<SwapRecord>()
        }
        val feed = PersistedAccountingEventFeedService(facts, trades, swaps, funding)
        val events = feed.findEvents(time.minusHours(1), time.plusHours(1), 10)
        assertThat(events.map { it.eventId }).containsExactlyInAnyOrder(done.eventId, "legacy")
    }
}
