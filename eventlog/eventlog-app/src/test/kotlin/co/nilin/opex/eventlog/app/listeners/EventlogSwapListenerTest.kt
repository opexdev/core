package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.core.swap.*
import co.nilin.opex.eventlog.app.service.PersistedAccountingEventFeedService
import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.math.BigDecimal
import java.time.LocalDateTime

class EventlogSwapListenerTest {
    private val time = LocalDateTime.parse("2026-11-18T10:00:00")
    private val event = SwapEvent(
        uuid = "customer", txId = "swap:reservation-1",
        sourceAsset = "USDT", sourceAmount = BigDecimal("10.000000000000000001"),
        destAsset = "IRT", destAmount = BigDecimal("1500000.01"),
        sourceTransactionId = "101", destinationTransactionId = "102",
        sourceReference = "reservation-1-withdraw", destinationReference = "reservation-1-deposit",
        systemPartyId = "system", receiverPartyId = "recipient", createDate = time,
        sourceSenderWalletId = "11", sourceReceiverWalletId = "21",
        destinationSenderWalletId = "22", destinationReceiverWalletId = "31",
        sourceSenderWalletType = "MAIN", sourceReceiverWalletType = "MAIN",
        destinationSenderWalletType = "MAIN", destinationReceiverWalletType = "MAIN",
        reservationId = 7, reservationReference = "reservation-1", quotedRate = BigDecimal("150000.001"),
        quotedAt = time.minusMinutes(1), quoteExpiresAt = time.plusMinutes(4),
        transferCategory = "IMPERSONATED_PURCHASE_FINALIZED", reservedBy = "quote-admin",
        executedBy = "executor", description = "OTC", status = "COMPLETED"
    )

    @Test
    fun `captures one full operation and derives OUT IN views with exact transaction and party links`() = runBlocking<Unit> {
        val store = Store()
        EventlogSwapListener(store, store).onEvent(event, 2, 10, 0)
        assertThat(store.operations).hasSize(1)
        assertThat(store.audit).isEmpty()
        val swap = store.operations.single()
        assertThat(store.position).isEqualTo(Triple("swap", 2, 10L))
        assertThat(swap.reservationId).isEqualTo(7)
        assertThat(swap.reservationReference).isEqualTo("reservation-1")
        assertThat(swap.quotedRate).isEqualByComparingTo(event.quotedRate)
        assertThat(swap.quotedAt).isEqualTo(event.quotedAt)
        assertThat(swap.quoteExpiresAt).isEqualTo(event.quoteExpiresAt)
        assertThat(swap.reservedBy).isEqualTo("quote-admin")
        assertThat(swap.executedBy).isEqualTo("executor")
        assertThat(swap.description).isEqualTo("OTC")
        assertThat(swap.status).isEqualTo("COMPLETED")
        val views = swap.toAccountingEvents()
        val out = views.first { it.side == "OUT" }
        val incoming = views.first { it.side == "IN" }
        assertThat(out.txId).isEqualTo("101")
        assertThat(incoming.txId).isEqualTo("102")
        assertThat(out.operationId).isEqualTo(incoming.operationId).isEqualTo(event.txId)
        assertThat(out.asset).isEqualTo("USDT")
        assertThat(out.amount).isEqualByComparingTo("10.000000000000000001")
        assertThat(incoming.asset).isEqualTo("IRT")
        assertThat(out.senderPartyId).isEqualTo("customer")
        assertThat(out.receiverPartyId).isEqualTo("system")
        assertThat(incoming.senderPartyId).isEqualTo("system")
        assertThat(incoming.receiverPartyId).isEqualTo("recipient")
        assertThat(incoming.perspectivePartyId).isEqualTo("recipient")
        assertThat(out.relatedTransactionId).isEqualTo(incoming.txId)
        assertThat(incoming.businessReference).isEqualTo("reservation-1-deposit")
        assertThat(out.fee).isNull()
        assertThat(out.price).isEqualByComparingTo(event.quotedRate)
        val movements = object : AccountingTransactionLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) =
                listOf(out.copy(eventId = "wallet-transfer:101", source = "CORE_WALLET", recordRole = "SWAP_SETTLEMENT"))
        }
        val feed = PersistedAccountingEventFeedService(movements, object : RichTradeLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<RichTradeRecord>()
        }, store, co.nilin.opex.eventlog.app.EmptyFundingReader)
        assertThat(feed.findEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(2)
        assertThat(feed.findRawEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(3)
    }

    @Test
    fun `real producer JSON retains every business field across consumer and canonical storage`() {
        val producerType = co.nilin.opex.wallet.ports.kafka.listener.model.SwapEvent::class.java
        fun fields(type: Class<*>) = type.declaredFields.filterNot {
            it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers)
        }.map { it.name }
        assertThat(fields(SwapEvent::class.java)).containsExactlyInAnyOrderElementsOf(fields(producerType))
        assertThat(fields(SwapRecord::class.java)).containsExactlyInAnyOrderElementsOf(fields(producerType))
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().findAndRegisterModules()
        val producer = mapper.readValue(mapper.writeValueAsBytes(event), producerType)
        val headers = RecordHeaders()
        val bytes = JsonSerializer<co.nilin.opex.wallet.ports.kafka.listener.model.SwapEvent>().serialize("swap", headers, producer)
        val decoded = JsonDeserializer(SwapEvent::class.java, false).deserialize("swap", headers, bytes)
        assertThat(decoded).isEqualTo(event)
    }

    @Test
    fun `stable leg identities survive replay and different operations remain distinct`() {
        val store = Store()
        val listener = EventlogSwapListener(store, store)
        listener.onEvent(event, 0, 1, 0)
        listener.onEvent(event, 0, 2, 0)
        assertThat(store.operations.flatMap { it.toAccountingEvents() }.map { it.eventId }.distinct()).hasSize(2)
        listener.onEvent(event.copy(txId = "swap:reservation-2", reservationReference = "reservation-2"), 0, 3, 0)
        assertThat(store.operations.map { it.txId }.distinct()).hasSize(2)
    }

    @Test
    fun `keeps incomplete historical same asset events as unresolved audit only`() {
        val store = Store()
        EventlogSwapListener(store, store).onEvent(event.copy(sourceTransactionId = null,
            destinationTransactionId = null, destAsset = "USDT"), 0, 1, 0)
        assertThat(store.operations).isEmpty()
        assertThat(store.audit.single().recordRole).isEqualTo("UNRESOLVED_SWAP")
        assertThat(store.audit.single().side).isNull()
    }

    @Test
    fun `rejects malformed complete operations before storage`() {
        val store = Store()
        val listener = EventlogSwapListener(store, store)
        listOf(event.copy(destinationTransactionId = "101"), event.copy(sourceAmount = BigDecimal.ZERO),
            event.copy(destAsset = "USDT"), event.copy(quotedRate = BigDecimal.ZERO),
            event.copy(status = "CREATED")).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) { listener.onEvent(invalid, 0, 1, 0) }
        }
        assertThat(store.operations).isEmpty()
    }

    private class Store : AccountingTransactionLogWriter, SwapLogWriter, SwapLogReader {
        val operations = mutableListOf<SwapRecord>()
        val audit = mutableListOf<AccountingTransactionEvent>()
        var position: Triple<String, Int, Long>? = null
        override suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent {
            audit.add(transaction)
            return transaction
        }
        override suspend fun saveAll(transactions: List<AccountingTransactionEvent>) = transactions.map { save(it) }
        override suspend fun save(swap: SwapRecord, topic: String, partition: Int, offset: Long): SwapRecord {
            operations.add(swap)
            position = Triple(topic, partition, offset)
            return swap
        }
        override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = operations
            .filter { !it.createDate.isBefore(from) && !it.createDate.isAfter(to) }.take(limit)
    }
}
