package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.ports.postgres.dao.RateHistoryRepository
import co.nilin.opex.price.ports.postgres.model.CrossRateSparkPoint
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDateTime

class CrossRateSparklineLoaderImplTest {

    private val rateHistoryRepository = mockk<RateHistoryRepository>()
    private val loader = CrossRateSparklineLoaderImpl(rateHistoryRepository)

    private fun point(symbol: String, t: LocalDateTime, price: String, changePercent: String, isTrendUp: Boolean) =
        CrossRateSparkPoint(symbol, t, BigDecimal(price), BigDecimal(changePercent), isTrendUp)

    @Test
    fun `rows are grouped by symbol, ordered by time within a group, and symbols are sorted`(): Unit = runBlocking {
        val t0 = LocalDateTime.now().minusHours(1)
        val t1 = t0.plusMinutes(30)

        // deliberately out of order: ETH before BTC, and BTC's later bucket before its earlier one
        every {
            rateHistoryRepository.findCrossRateSparkPoints("USDT", "USDT", t0, t1, 2)
        } returns Flux.just(
            point("ETH-USDT", t0, "2000", "5.00", true),
            point("BTC-USDT", t1, "51000", "2.00", true),
            point("ETH-USDT", t1, "2100", "5.00", true),
            point("BTC-USDT", t0, "50000", "2.00", true),
        )

        val result = loader.loadSparklines("USDT", "USDT", t0, t1, 2)

        assertThat(result.map { it.symbol }).containsExactly("BTC-USDT", "ETH-USDT")

        val btc = result.first { it.symbol == "BTC-USDT" }
        assertThat(btc.times).containsExactly(t0, t1)
        assertThat(btc.prices).containsExactly(BigDecimal("50000"), BigDecimal("51000"))
        assertThat(btc.isTrendUp).isTrue()
        assertThat(btc.changePercent).isEqualByComparingTo(BigDecimal("2.00"))

        val eth = result.first { it.symbol == "ETH-USDT" }
        assertThat(eth.times).containsExactly(t0, t1)
        assertThat(eth.prices).containsExactly(BigDecimal("2000"), BigDecimal("2100"))

        verify(exactly = 1) { rateHistoryRepository.findCrossRateSparkPoints("USDT", "USDT", t0, t1, 2) }
    }

    @Test
    fun `no rows from the repository yields an empty list`(): Unit = runBlocking {
        every {
            rateHistoryRepository.findCrossRateSparkPoints(any(), any(), any(), any(), any())
        } returns Flux.empty()

        val result = loader.loadSparklines("USDT", "USDT", LocalDateTime.now().minusHours(1), LocalDateTime.now(), 24)

        assertThat(result).isEmpty()
    }

    @Test
    fun `arguments are forwarded to the repository unchanged`(): Unit = runBlocking {
        val start = LocalDateTime.now().minusDays(1)
        val end = LocalDateTime.now()
        every {
            rateHistoryRepository.findCrossRateSparkPoints("IRT", "USDT", start, end, 30)
        } returns Flux.empty()

        loader.loadSparklines(refCurrency = "IRT", hub = "USDT", startTime = start, endTime = end, points = 30)

        verify(exactly = 1) { rateHistoryRepository.findCrossRateSparkPoints("IRT", "USDT", start, end, 30) }
    }
}
