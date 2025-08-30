package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.RateSource
import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.dao.UserTradeVolumeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import co.nilin.opex.market.ports.postgres.model.CurrencyRateModel
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class TradePersisterTest {

    private val tradeRepository = mockk<TradeRepository>()
    private val currencyRateRepository = mockk<CurrencyRateRepository>()
    private val cacheHelper = mockk<RedisCacheHelper>()
    private val tradeVolumeRepository = mockk<UserTradeVolumeRepository>()
    private val tradePersister =
        TradePersisterImpl(tradeRepository, currencyRateRepository, cacheHelper, tradeVolumeRepository)

    @Test
    fun givenTradeRepo_whenSaveRichTrade_thenSuccess(): Unit = runBlocking {
        every { tradeRepository.save(any()) } returns Mono.just(VALID.TRADE_MODEL)
        every { currencyRateRepository.createOrUpdate(any(), any(), any(), any()) } returns Mono.empty()
        every { currencyRateRepository.findByBaseAndQuoteAndSource(any(), any(), any()) } returns Mono.just(
            CurrencyRateModel(1, "BTC", "USDT", RateSource.EXTERNAL, 1.toBigDecimal())
        )
        every { tradeVolumeRepository.insertOrUpdate(any(), any(), any(), any(), any(), any()) } returns Mono.empty()

        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(VALID.RICH_TRADE) } }
    }
}
