package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import reactor.core.publisher.Mono
import java.math.BigDecimal

class TradePersisterTest {

    private val tradeRepository = mockk<TradeRepository>()
    private val cacheHelper = mockk<RedisCacheHelper>()
    private val tradePersister =
        TradePersisterImpl(tradeRepository, cacheHelper)

    @Test
    fun givenTradeRepo_whenSaveRichTrade_thenSuccess(): Unit = runBlocking {
        every { tradeRepository.save(any()) } returns Mono.just(VALID.TRADE_MODEL)
        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(VALID.RICH_TRADE) } }
    }

    @Test
    fun givenDuplicateTrade_whenSaveRichTrade_thenIgnoredAsIdempotent(): Unit = runBlocking {
        every { tradeRepository.save(any()) } returnsMany listOf(
            Mono.error(DuplicateKeyException("Duplicate key")),
            Mono.just(VALID.TRADE_MODEL)
        )
        every { tradeRepository.findBySymbolAndTradeId(any(), any()) } returns Mono.just(VALID.TRADE_MODEL)
        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(VALID.RICH_TRADE) } }
    }

    @Test
    fun givenTradeIdCollision_whenSaveRichTrade_thenThrow() {
        every { tradeRepository.save(any()) } returns Mono.error(DuplicateKeyException("duplicate trade"))
        every { tradeRepository.findBySymbolAndTradeId(any(), any()) } returns Mono.just(
            TradeModel(
                VALID.TRADE_MODEL.id,
                VALID.TRADE_MODEL.tradeId,
                VALID.TRADE_MODEL.symbol,
                VALID.TRADE_MODEL.baseAsset,
                VALID.TRADE_MODEL.quoteAsset,
                VALID.TRADE_MODEL.matchedPrice,
                VALID.TRADE_MODEL.matchedQuantity.add(BigDecimal.ONE),
                VALID.TRADE_MODEL.takerPrice,
                VALID.TRADE_MODEL.makerPrice,
                VALID.TRADE_MODEL.takerCommission,
                VALID.TRADE_MODEL.makerCommission,
                VALID.TRADE_MODEL.takerCommissionAsset,
                VALID.TRADE_MODEL.makerCommissionAsset,
                VALID.TRADE_MODEL.tradeDate,
                VALID.TRADE_MODEL.makerOuid,
                VALID.TRADE_MODEL.takerOuid,
                VALID.TRADE_MODEL.makerUuid,
                VALID.TRADE_MODEL.takerUuid,
                VALID.TRADE_MODEL.createDate
            )
        )

        assertThrows<DuplicateKeyException> {
            runBlocking { tradePersister.save(VALID.RICH_TRADE) }
        }
    }
}
