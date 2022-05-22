package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.event.RichTrade
import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.model.TradeModel
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Mono
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
        stubbing(tradeRepository) {
            on {
                save(any())
            } doReturn Mono.just(
                TradeModel(
                    1,
                    1,
                    "ETH_USDT",
                    0.001,
                    100000.0,
                    100000.0,
                    0.001,
                    0.001,
                    "",
                    "",
                    LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC),
                    "99289106-2775-44d4-bffc-ca35fc25e58c",
                    "2fa73fa2-6d70-44b8-8571-e2b24e2eea2b",
                    "52c6d890-3dd4-4fa8-9425-d9e0d6274705",
                    "07bb979a-dfca-475b-a38b-fcc5dd2f88d8",
                    LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC)
                )
            )
        }

        assertThatNoException().isThrownBy { runBlocking { tradePersister.save(richOrder) } }
    }
}
