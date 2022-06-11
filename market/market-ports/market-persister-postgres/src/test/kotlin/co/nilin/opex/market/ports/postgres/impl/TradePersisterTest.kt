package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class TradePersisterTest {
    private val tradeRepository: TradeRepository = mockk()
    private val tradePersister = TradePersisterImpl(tradeRepository)

    @Test
    fun givenTradeRepo_whenSaveRichTrade_thenSuccess(): Unit = runBlocking {
        every {
            tradeRepository.save(any())
        } returns Mono.just(VALID.TRADE_MODEL)

        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(VALID.RICH_TRADE) } }
    }
}
