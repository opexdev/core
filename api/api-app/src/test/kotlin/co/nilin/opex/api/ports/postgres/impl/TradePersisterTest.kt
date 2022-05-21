package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset

class TradePersisterTest {
    private val tradeRepository: TradeRepository = mock()
    private val tradePersister = TradePersisterImpl(tradeRepository)

    @Test
    fun givenRichTrade_whenSave_thenSuccess(): Unit = runBlocking {
        val richOrder = RichTrade(
            1, // ?
            "ETH_USDT",
            "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
            "18013d13-0568-496b-b93b-2524c8132b93",
            1,
            OrderDirection.ASK,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            "", // ?
            "26931efc-891b-4599-9921-1d265829b410",
            "5296a097-6478-464f-91a6-5c434ac4207d",
            2,
            OrderDirection.ASK,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(0.01),
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            BigDecimal.valueOf(0), // ?
            "", // ?
            BigDecimal.valueOf(0), // ?
            LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC)
        )

        Assertions.assertThatThrownBy { runBlocking { tradePersister.save(richOrder) } }.doesNotThrowAnyException()
    }
}
