package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.CurrencyRateRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
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
    private val tradePersister = TradePersisterImpl(tradeRepository, currencyRateRepository, cacheHelper)

    @Test
    fun givenTradeRepo_whenSaveRichTrade_thenSuccess(): Unit = runBlocking {
        every { tradeRepository.save(any()) } returns Mono.just(VALID.TRADE_MODEL)
        every { currencyRateRepository.createOrUpdate(any(), any(), any(), any()) } returns Mono.empty()

        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(VALID.RICH_TRADE) } }
    }
}
