package co.nilin.opex.eventlog.ports.postgres.impl

import co.nilin.opex.eventlog.core.trade.RichTradeRecord
import co.nilin.opex.eventlog.ports.postgres.dao.RichTradeRepository
import co.nilin.opex.eventlog.ports.postgres.model.RichTradeModel
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class RichTradeLogPersisterTest {
    @Test
    fun `preserves complete execution and source position and accepts numerically identical replay`() = runBlocking<Unit> {
        val repository = mockk<RichTradeRepository>()
        val persister = RichTradeLogPersister(repository)
        val captured = slot<RichTradeModel>()
        every { repository.save(capture(captured)) } answers { Mono.just(firstArg<RichTradeModel>().copy(id = 1L)) }
        val source = sample()
        val saved = persister.save(source, "richTrade", 2, 10)
        val model = captured.captured.copy(id = 1L)
        assertThat(model.sourceTopic).isEqualTo("richTrade")
        assertThat(model.sourcePartition).isEqualTo(2)
        assertThat(model.sourceOffset).isEqualTo(10)
        assertThat(saved.takerOrderId).isEqualTo(11L)
        assertThat(saved.makerOrderId).isEqualTo(12L)
        assertThat(saved.takerQuantity).isEqualByComparingTo("5")
        assertThat(saved.makerQuantity).isEqualByComparingTo("2")
        assertThat(saved.takerRemainedQuantity).isEqualByComparingTo("4.875")
        assertThat(saved.makerRemainedQuantity).isEqualByComparingTo("1.875")
        assertThat(saved.matchedQuantity).isEqualByComparingTo("0.125000000000000001")
        assertThat(saved.takerPrice).isEqualByComparingTo("20000")
        assertThat(saved.matchedPrice).isEqualByComparingTo("19000")
        assertThat(saved.makerFeeActionId).isEqualTo("maker-fee")
        assertThat(saved.takerSettlementActionId).isEqualTo("taker-settlement")

        every { repository.save(any<RichTradeModel>()) } returns Mono.error(DuplicateKeyException("duplicate execution"))
        every { repository.findByExecutionReference(source.executionReference) } returns Mono.just(model)
        val replay = persister.save(source.copy(matchedPrice = BigDecimal("19000.0000")), "richTrade", 3, 20)
        assertThat(replay).isEqualTo(saved)
        assertThrows(IllegalStateException::class.java) {
            runBlocking { persister.save(source.copy(matchedQuantity = BigDecimal("0.2")), "richTrade", 3, 21) }
        }
    }

    @Test
    fun `same matching trade number is allowed for different execution references`() = runBlocking<Unit> {
        val repository = mockk<RichTradeRepository>()
        val persister = RichTradeLogPersister(repository)
        every { repository.save(any<RichTradeModel>()) } answers { Mono.just(firstArg<RichTradeModel>().copy(id = 1L)) }
        val first = persister.save(sample(), "richTrade", 0, 1)
        val second = persister.save(sample().copy(executionReference = "another-execution"), "richTrade", 0, 2)
        assertThat(first.originalTradeId).isEqualTo(second.originalTradeId)
        assertThat(first.executionReference).isNotEqualTo(second.executionReference)
    }

    private fun sample() = RichTradeRecord(
        executionReference = "execution-1", originalTradeId = 1, pair = "BTC_USDT",
        takerOuid = "taker-order", takerUuid = "taker", takerOrderId = 11, takerDirection = "BID",
        takerPrice = BigDecimal("20000"), takerQuantity = BigDecimal("5"),
        takerQuoteQuantity = BigDecimal("100000"), takerRemainedQuantity = BigDecimal("4.875"),
        takerCommision = BigDecimal("0.000125"), takerCommisionAsset = "BTC",
        makerOuid = "maker-order", makerUuid = "maker", makerOrderId = 12, makerDirection = "ASK",
        makerPrice = BigDecimal("19000"), makerQuantity = BigDecimal("2"),
        makerQuoteQuantity = BigDecimal("38000"), makerRemainedQuantity = BigDecimal("1.875"),
        makerCommision = BigDecimal("2.375"), makerCommisionAsset = "USDT",
        matchedPrice = BigDecimal("19000"), matchedQuantity = BigDecimal("0.125000000000000001"),
        tradeDateTime = LocalDateTime.parse("2026-11-18T10:00:00"), baseAsset = "BTC", quoteAsset = "USDT",
        takerSettlementActionId = "taker-settlement", makerSettlementActionId = "maker-settlement",
        takerFeeActionId = "taker-fee", makerFeeActionId = "maker-fee"
    )
}
