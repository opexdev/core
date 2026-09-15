package co.nilin.opex.price.ports.postgres.dao

import co.nilin.opex.price.ports.postgres.model.CrossRateSparkPoint
import co.nilin.opex.price.ports.postgres.model.RateHistoryModel
import co.nilin.opex.price.ports.postgres.support.PostgresTestApp
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

@Testcontainers
@SpringBootTest(classes = [PostgresTestApp::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CrossRateSparkPointsQueryTest {

    @Autowired
    private lateinit var rateHistoryRepository: RateHistoryRepository

    @BeforeEach
    fun cleanRateHistory(): Unit = runBlocking {
        rateHistoryRepository.deleteAll().awaitFirstOrNull()
    }

    private suspend fun seed(symbol: String, price: String, at: LocalDateTime) {
        rateHistoryRepository.save(
            RateHistoryModel(symbol = symbol, price = BigDecimal(price), createdDate = at, source = "AUTO")
        ).awaitSingle()
    }

    private suspend fun query(
        refCurrency: String,
        hub: String,
        start: LocalDateTime,
        end: LocalDateTime,
        points: Int,
    ): List<CrossRateSparkPoint> =
        rateHistoryRepository.findCrossRateSparkPoints(refCurrency, hub, start, end, points)
            .collectList()
            .awaitSingle()

    private fun List<CrossRateSparkPoint>.bySymbol(symbol: String) =
        filter { it.symbol == symbol }.sortedBy { it.bucketTime }

    private fun assertCloseTo(actual: LocalDateTime, expected: LocalDateTime, within: Duration = Duration.ofSeconds(2)) {
        val gap = Duration.between(expected, actual).let { if (it.isNegative) it.negated() else it }
        assertThat(gap).isLessThanOrEqualTo(within)
    }

    private fun assertApprox(actual: BigDecimal, expected: BigDecimal, relativeTolerance: BigDecimal = BigDecimal("0.0001")) {
        val diff = (expected - actual).abs()
        val allowed = expected.abs() * relativeTolerance
        assertThat(diff).describedAs("expected ~%s but was %s (diff=%s, allowed=%s)", expected, actual, diff, allowed)
            .isLessThanOrEqualTo(allowed)
    }

    @Test
    fun `direct pair priced in the hub itself is a flat line when only one tick exists`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "50000", start.minusMinutes(5))

        val rows = query(refCurrency = "USDT", hub = "USDT", start = start, end = end, points = 4)

        val btc = rows.bySymbol("BTC-USDT")
        assertThat(btc).hasSize(4)
        btc.forEach { assertThat(it.price).isEqualByComparingTo(BigDecimal("50000")) }
        assertThat(btc.first().isTrendUp).isTrue()
        assertThat(btc.first().changePercent).isEqualByComparingTo(BigDecimal("0.00"))
        assertCloseTo(btc.first().bucketTime, start)
        assertCloseTo(btc.last().bucketTime, end)
        assertThat(btc.map { it.bucketTime }).isSorted

        // the reference currency itself must never be paired with itself
        assertThat(rows.map { it.symbol }).doesNotContain("USDT-USDT")
    }

    @Test
    fun `inverse pair is resolved as 1 over price`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        // 1 BTC = 5000 USDT, stored the other way around
        seed("USDT-BTC", "0.0002", start.minusMinutes(5))

        val rows = query(refCurrency = "USDT", hub = "USDT", start = start, end = end, points = 3)

        val btc = rows.bySymbol("BTC-USDT")
        assertThat(btc).hasSize(3)
        btc.forEach { assertApprox(it.price, BigDecimal("5000")) }
    }

    @Test
    fun `bridge via the hub combines two legs and also derives hub-to-ref`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "50000", start.minusMinutes(5))
        // 1 USDT = 950000 IRT
        seed("USDT-IRT", "950000", start.minusMinutes(5))

        val rows = query(refCurrency = "IRT", hub = "USDT", start = start, end = end, points = 3)

        // the hub itself must show up as <hub>-<ref>, reconstructed purely from the bridge leg
        val usdtIrt = rows.bySymbol("USDT-IRT")
        assertThat(usdtIrt).hasSize(3)
        usdtIrt.forEach { assertApprox(it.price, BigDecimal("950000")) }

        // BTC-IRT = (BTC in USDT) / (IRT in USDT) = 50000 / (1 / 950000) = 50000 * 950000
        val btcIrt = rows.bySymbol("BTC-IRT")
        assertThat(btcIrt).hasSize(3)
        btcIrt.forEach { assertApprox(it.price, BigDecimal("47500000000")) }
    }

    @Test
    fun `price carries forward and updates once a newer tick appears`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "100", start.minusMinutes(10)) // seeds bucket 0 and 1 (before midpoint)
        seed("BTC-USDT", "120", start.plusHours(1).plusMinutes(1)) // just after the midpoint bucket

        val btc = query(refCurrency = "USDT", hub = "USDT", start = start, end = end, points = 3).bySymbol("BTC-USDT")

        assertThat(btc).hasSize(3)
        assertThat(btc.map { it.price }).containsExactly(
            BigDecimal("100"), BigDecimal("100"), BigDecimal("120")
        )
        assertThat(btc.all { it.isTrendUp }).isTrue()
        btc.forEach { assertThat(it.changePercent).isEqualByComparingTo(BigDecimal("20.00")) }
    }

    @Test
    fun `symbol is omitted entirely when the reference currency has no history`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "50000", start.minusMinutes(5))

        val rows = query(refCurrency = "XYZ", hub = "USDT", start = start, end = end, points = 3)

        assertThat(rows).isEmpty()
    }

    @Test
    fun `symbol and refCurrency matching is case-insensitive and output is upper-cased`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("btc-usdt", "42000", start.minusMinutes(5))

        val rows = query(refCurrency = "usdt", hub = "usdt", start = start, end = end, points = 2)

        val btc = rows.bySymbol("BTC-USDT")
        assertThat(btc).hasSize(2)
        btc.forEach { assertThat(it.price).isEqualByComparingTo(BigDecimal("42000")) }
    }

    @Test
    fun `points is clamped to a minimum of two`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "50000", start.minusMinutes(5))

        val btc = query(refCurrency = "USDT", hub = "USDT", start = start, end = end, points = 1)
            .bySymbol("BTC-USDT")

        assertThat(btc).hasSize(2)
    }

    @Test
    fun `multiple assets are returned independently with their own trend`(): Unit = runBlocking {
        val now = LocalDateTime.now()
        val start = now.minusHours(2)
        val end = now
        seed("BTC-USDT", "100", start.minusMinutes(5)) // flat
        seed("ETH-USDT", "200", start.minusMinutes(10)) // trending down
        seed("ETH-USDT", "150", start.plusHours(1).plusMinutes(1))

        val rows = query(refCurrency = "USDT", hub = "USDT", start = start, end = end, points = 3)

        val btc = rows.bySymbol("BTC-USDT")
        assertThat(btc.first().isTrendUp).isTrue()
        assertThat(btc.first().changePercent).isEqualByComparingTo(BigDecimal("0.00"))

        val eth = rows.bySymbol("ETH-USDT")
        assertThat(eth.first().isTrendUp).isFalse()
        assertThat(eth.first().changePercent).isEqualByComparingTo(BigDecimal("-25.00"))
    }

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:14.9"))
            .withDatabaseName("opex_test")
            .withUsername("opex")
            .withPassword("hiopex")

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${postgres.databaseName}"
            }
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
