package co.nilin.opex.eventlog.app.listeners

import co.nilin.opex.accountant.core.inout.RichTrade
import co.nilin.opex.eventlog.core.accounting.*
import co.nilin.opex.eventlog.core.trade.*
import co.nilin.opex.eventlog.core.swap.SwapLogReader
import co.nilin.opex.eventlog.core.swap.SwapRecord
import co.nilin.opex.eventlog.app.service.PersistedAccountingEventFeedService
import co.nilin.opex.eventlog.ports.kafka.listener.inout.RichTradeEvent
import co.nilin.opex.matching.engine.core.model.OrderDirection
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import java.math.BigDecimal
import java.time.LocalDateTime

class EnrichedTradeCaptureTest {
    @Test
    fun `transport contract captures every producer rich trade field`() {
        fun fields(type: Class<*>) = type.declaredFields
            .filterNot { it.isSynthetic || java.lang.reflect.Modifier.isStatic(it.modifiers) }
            .map { it.name }
        assertThat(fields(RichTradeEvent::class.java))
            .containsExactlyInAnyOrderElementsOf(fields(RichTrade::class.java))
    }

    private val time = LocalDateTime.parse("2026-11-18T10:00:00")
    private fun trade(market: String = "BTC_USDT", execution: String = "accountant-trade:taker-action") = RichTrade(
        id = 1L, pair = market, takerOuid = "taker-order", takerUuid = "taker", takerOrderId = 11L,
        takerDirection = OrderDirection.BID, takerPrice = BigDecimal("20000"),
        takerQuantity = BigDecimal("5"), takerQuoteQuantity = BigDecimal("100000"),
        takerRemainedQuantity = BigDecimal("4.875"), takerCommision = BigDecimal("0.000125"),
        takerCommisionAsset = "BTC", makerOuid = "maker-order", makerUuid = "maker", makerOrderId = 12L,
        makerDirection = OrderDirection.ASK, makerPrice = BigDecimal("19000"),
        makerQuantity = BigDecimal("2"), makerQuoteQuantity = BigDecimal("38000"),
        makerRemainedQuantity = BigDecimal("1.875"), makerCommision = BigDecimal("2.375"),
        makerCommisionAsset = "USDT", matchedPrice = BigDecimal("19000"),
        matchedQuantity = BigDecimal("0.125000000000000001"), tradeDateTime = time,
        executionReference = execution, baseAsset = "BTC", quoteAsset = market.substringAfter('_'),
        takerSettlementActionId = "taker-action", makerSettlementActionId = "maker-action",
        takerFeeActionId = "taker-fee", makerFeeActionId = "maker-fee"
    )

    private fun decode(source: RichTrade): RichTradeEvent {
        val headers = RecordHeaders()
        val serializer = JsonSerializer<RichTrade>()
        serializer.configure(mapOf(JsonSerializer.TYPE_MAPPINGS to "rich_trade:co.nilin.opex.accountant.core.inout.RichTrade"), false)
        val bytes = serializer.serialize("richTrade", headers, source)
        return JsonDeserializer(RichTradeEvent::class.java, false).deserialize("richTrade", headers, bytes)
    }

    @Test
    fun `captures scaled fill values and separate directions through real producer JSON`() {
        listOf("BTC_USDT", "BTC_IRT").forEach { market ->
            val rows = Store()
            val source = trade(market)
            EventlogRichTradeListener(rows).onMessage(ConsumerRecord("richTrade", 0, 5L, "key", decode(source)))
            assertThat(rows.trades).hasSize(1)
            assertThat(rows.trades.single().takerOrderId).isEqualTo(11L)
            assertThat(rows.trades.single().makerOrderId).isEqualTo(12L)
            assertThat(rows.trades.single().takerQuantity).isEqualByComparingTo("5")
            assertThat(rows.trades.single().takerPrice).isEqualByComparingTo("20000")
            assertThat(rows.trades.single().matchedQuantity).isEqualByComparingTo(source.matchedQuantity)
            assertThat(rows.events).hasSize(2)
            val taker = rows.events.first { it.participantRole == "TAKER" }
            val maker = rows.events.first { it.participantRole == "MAKER" }
            assertThat(rows.events.map { it.type }).containsOnly(AccountingTransactionType.TRADE)
            assertThat(taker.quantity).isEqualByComparingTo(source.matchedQuantity)
            assertThat(taker.price).isEqualByComparingTo("19000")
            assertThat(taker.quoteAmount).isEqualByComparingTo(source.matchedQuantity.multiply(source.matchedPrice))
            assertThat(taker.asset).isEqualTo("BTC")
            assertThat(taker.quoteAsset).isEqualTo(market.substringAfter('_'))
            assertThat(taker.tradeSide).isEqualTo("BUY")
            assertThat(maker.tradeSide).isEqualTo("SELL")
            assertThat(taker.side).isEqualTo("IN")
            assertThat(maker.side).isEqualTo("OUT")
            assertThat(taker.orderReference).isEqualTo("taker-order")
            assertThat(taker.financialActionId).isEqualTo("taker-action")
            assertThat(taker.relatedFinancialActionId).isEqualTo("maker-action")
            assertThat(taker.feeActionId).isEqualTo("taker-fee")
            assertThat(taker.fee).isNull()
            assertThat(taker.expectedFee).isEqualByComparingTo("0.000125")
        }
    }

    @Test
    fun `sell taker and buy maker preserve opposite asset directions`() {
        val store = Store()
        val source = decode(trade()).copy(takerDirection = "ASK", makerDirection = "BID")
        EventlogRichTradeListener(store).onMessage(ConsumerRecord("richTrade", 0, 5L, "key", source))
        val taker = store.events.first { it.participantRole == "TAKER" }
        val maker = store.events.first { it.participantRole == "MAKER" }
        assertThat(taker.tradeSide).isEqualTo("SELL")
        assertThat(taker.sourceAsset).isEqualTo("BTC")
        assertThat(taker.destinationAsset).isEqualTo("USDT")
        assertThat(maker.tradeSide).isEqualTo("BUY")
        assertThat(maker.sourceAsset).isEqualTo("USDT")
        assertThat(maker.destinationAsset).isEqualTo("BTC")
    }

    @Test
    fun `feed derives participant rows from the single complete execution source`() = runBlocking<Unit> {
        val store = Store()
        EventlogRichTradeListener(store).onMessage(ConsumerRecord("richTrade", 0, 1L, "key", decode(trade())))
        val emptyMovements = object : AccountingTransactionLogReader {
            override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<AccountingTransactionEvent>()
        }
        val feed = PersistedAccountingEventFeedService(emptyMovements, store, object : SwapLogReader {
    override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) = emptyList<SwapRecord>()
}, co.nilin.opex.eventlog.app.EmptyFundingReader)
        assertThat(store.trades).hasSize(1)
        assertThat(feed.findEvents(time.minusHours(1), time.plusHours(1), 10)).hasSize(2)
    }

    @Test
    fun `repeated numeric trade id does not collapse unrelated executions`() {
        val store = Store()
        val listener = EventlogRichTradeListener(store)
        listener.onMessage(ConsumerRecord("richTrade", 0, 1L, "key", decode(trade(execution = "accountant-trade:first-action"))))
        listener.onMessage(ConsumerRecord("richTrade", 0, 2L, "key", decode(trade(execution = "accountant-trade:second-action"))))
        assertThat(store.events.map { it.originalTradeId }).containsOnly("1")
        assertThat(store.events.map { it.eventId }.distinct()).hasSize(4)
    }

    private class Store : RichTradeLogWriter, RichTradeLogReader {
        val trades = mutableListOf<RichTradeRecord>()
        val events get() = trades.flatMap { it.toAccountingEvents() }
        override suspend fun save(trade: RichTradeRecord, topic: String, partition: Int, offset: Long): RichTradeRecord {
            trades.add(trade)
            return trade
        }
        override suspend fun findBetween(from: LocalDateTime, to: LocalDateTime, limit: Int) =
            trades.filter { !it.tradeDateTime.isBefore(from) && !it.tradeDateTime.isAfter(to) }.take(limit)
    }
}
