package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.app.source.PersistedAccountingEventSource
import co.nilin.opex.eventlog.ports.kafka.listener.inout.SwapEvent
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class EventlogSwapListenerTest {
    private val time = LocalDateTime.parse("2026-11-17T10:00:00")
    private val event = SwapEvent(
        uuid = "customer", txId = "swap:reservation-1",
        sourceAsset = "USDT", sourceAmount = BigDecimal("10"),
        destAsset = "IRT", destAmount = BigDecimal("1500000"),
        sourceTransactionId = "101", destinationTransactionId = "102",
        sourceReference = "reservation-1-withdraw", destinationReference = "reservation-1-deposit",
        systemPartyId = "system", receiverPartyId = "customer", createDate = time
    )

    @Test
    fun `reports both customer legs with separate transaction and party references`() = runBlocking<Unit> {
        val store = Store()
        EventlogSwapListener(store).onEvent(event, 0, 1, 0)
        val out = store.rows.first { it.side == "OUT" }
        val incoming = store.rows.first { it.side == "IN" }
        assertThat(out.txId).isEqualTo("101")
        assertThat(incoming.txId).isEqualTo("102")
        assertThat(out.operationId).isEqualTo(incoming.operationId).isEqualTo(event.txId)
        assertThat(out.asset).isEqualTo("USDT")
        assertThat(incoming.asset).isEqualTo("IRT")
        assertThat(out.senderPartyId).isEqualTo("customer")
        assertThat(out.receiverPartyId).isEqualTo("system")
        assertThat(incoming.senderPartyId).isEqualTo("system")
        assertThat(incoming.receiverPartyId).isEqualTo("customer")
        assertThat(incoming.perspectivePartyId).isEqualTo("customer")
        assertThat(out.relatedTransactionId).isEqualTo(incoming.txId)
        assertThat(incoming.businessReference).isEqualTo("reservation-1-deposit")
        assertThat(out.fee).isNull()
        val source = PersistedAccountingEventSource(object : TradeReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<TradeRecord>()
        }, object : AccountingTransactionLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) =
                (store.rows + out.copy(txId = "settlement-101", recordRole = "SWAP_SETTLEMENT")).take(limit)
        })
        assertThat(source.collectEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(2)
    }

    @Test
    fun `replayed operation keeps stable leg identities and supports a different recipient`() {
        val store = Store()
        val listener = EventlogSwapListener(store)
        listener.onEvent(event.copy(receiverPartyId = "recipient"), 0, 1, 0)
        listener.onEvent(event.copy(receiverPartyId = "recipient"), 0, 2, 0)
        assertThat(store.rows.map { it.eventId }.distinct()).hasSize(2)
        assertThat(store.rows.first { it.side == "IN" }.userId).isEqualTo("recipient")
    }

    @Test
    fun `does not invent a complete swap from an old same currency transfer message`() {
        val store = Store()
        EventlogSwapListener(store).onEvent(event.copy(sourceTransactionId = null, destinationTransactionId = null), 0, 1, 0)
        assertThat(store.rows).hasSize(1)
        assertThat(store.rows.single().recordRole).isEqualTo("UNRESOLVED_SWAP")
        assertThat(store.rows.single().side).isNull()
    }

    private class Store : AccountingTransactionLogWriter {
        val rows = mutableListOf<AccountingTransactionEvent>()
        override suspend fun save(transaction: AccountingTransactionEvent): AccountingTransactionEvent {
            rows.add(transaction)
            return transaction
        }
        override suspend fun saveAll(transactions: List<AccountingTransactionEvent>) = transactions.map { save(it) }
    }
}
