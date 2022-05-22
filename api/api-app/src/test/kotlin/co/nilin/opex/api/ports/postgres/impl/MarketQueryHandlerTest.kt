package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.model.OrderModel
import co.nilin.opex.api.ports.postgres.model.OrderStatusModel
import co.nilin.opex.api.ports.postgres.model.TradeModel
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class MarketQueryHandlerTest {
    private val orderRepository: OrderRepository = mock()
    private val tradeRepository: TradeRepository = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val symbolMapper: SymbolMapper = mock()
    private val marketQueryHandler =
        MarketQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository, symbolMapper)

    @Test
    fun givenSymbol_whenOpenASKOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findBySymbolAndDirectionAndStatusSortAscendingByPrice("ETH_USDT", OrderDirection.BID, 1, listOf(0))
            } doReturn Flux.just(
                AggregatedOrderPriceModel(
                    100000.0,
                    0.001
                )
            )
        }
        val orderBookResponses = marketQueryHandler.openAskOrders("ETH_USDT", 10)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(
            OrderBookResponse(
                BigDecimal.valueOf(100000.0),
                BigDecimal.valueOf(0.001)
            )
        )
    }

    @Test
    fun givenSymbol_whenOpenBIDOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findBySymbolAndDirectionAndStatusSortDescendingByPrice("ETH_USDT", OrderDirection.BID, 1, listOf(0))
            } doReturn Flux.just(
                AggregatedOrderPriceModel(
                    100000.0,
                    0.001
                )
            )
        }
        val orderBookResponses = marketQueryHandler.openBidOrders("ETH_USDT", 10)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(
            OrderBookResponse(
                BigDecimal.valueOf(100000.0),
                BigDecimal.valueOf(0.001)
            )
        )
    }

    @Test
    fun givenSymbol_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findLastOrderBySymbol("ETH_USDT")
            } doReturn Mono.just(
                OrderModel(
                    1,
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    "18013d13-0568-496b-b93b-2524c8132b93",
                    "id", // ?
                    "ETH_USDT",
                    1,
                    0.01,
                    0.01,
                    0.0001,
                    0.01,
                    "1",
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    MatchingOrderType.LIMIT_ORDER,
                    100000.0,
                    0.01,
                    0.0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }
        stubbing(orderStatusRepository) {
            on {
                findMostRecentByOUID("f1167d30-ccc0-4f86-ab5d-dd24aa3250df")
            } doReturn Mono.just(
                OrderStatusModel(
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    0.0, // ?
                    0.0, // ?
                    0, // ?
                    0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }

        val queryOrderResponse = marketQueryHandler.lastOrder("ETH_USDT")

        assertThat(queryOrderResponse).isNotNull
        assertThat(queryOrderResponse).isEqualTo(
            QueryOrderResponse(
                "ETH_USDT",
                "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                1,
                1, // ?,
                "id",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.001),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(1),
                OrderStatus.FILLED,
                TimeInForce.GTC,
                OrderType.LIMIT,
                OrderSide.BUY,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(100000),
                Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
                Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
                true,
                BigDecimal.valueOf(0)
            )
        )
    }

    @Test
    fun givenSymbol_whenLastPrice_thenPriceTickerResponse(): Unit = runBlocking {
        stubbing(tradeRepository) {
            on {
                findAllGroupBySymbol()
            } doReturn Flux.just(
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
            on {
                findBySymbolGroupBySymbol("ETH_USDT")
            } doReturn Flux.just(
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
        stubbing(orderRepository) {
            on {
                findByOuid("99289106-2775-44d4-bffc-ca35fc25e58c")
            } doReturn Mono.just(
                OrderModel(
                    1,
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    "18013d13-0568-496b-b93b-2524c8132b93",
                    "id", // ?
                    "ETH_USDT",
                    1,
                    0.01,
                    0.01,
                    0.0001,
                    0.01,
                    "1",
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    MatchingOrderType.LIMIT_ORDER,
                    100000.0,
                    0.01,
                    0.0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }
        val priceTickerResponse = marketQueryHandler.lastPrice("ETH_USDT")

        assertThat(priceTickerResponse).isNotNull
        assertThat(priceTickerResponse.size).isEqualTo(1)
        assertThat(priceTickerResponse.first().symbol).isEqualTo("ETH_USDT")
        assertThat(priceTickerResponse.first().price).isEqualTo(100000)
    }

    @Test
    fun givenSymbol_whenRecentTrades_thenMarketTradeResponseFlow(): Unit = runBlocking {
        stubbing(tradeRepository) {
            on {
                findBySymbolSortDescendingByCreateDate("ETH_USDT", 10)
            } doReturn flow {
                emit(
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
        }
        stubbing(orderRepository) {
            on {
                findByOuid("99289106-2775-44d4-bffc-ca35fc25e58c")
            } doReturn Mono.just(
                OrderModel(
                    1,
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    "18013d13-0568-496b-b93b-2524c8132b93",
                    "id", // ?
                    "ETH_USDT",
                    1,
                    0.01,
                    0.01,
                    0.0001,
                    0.01,
                    "1",
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    MatchingOrderType.LIMIT_ORDER,
                    100000.0,
                    0.01,
                    0.0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
            on {
                findByOuid("2fa73fa2-6d70-44b8-8571-e2b24e2eea2b")
            } doReturn Mono.just(
                OrderModel(
                    1,
                    "f1167d30-ccc0-4f86-ab5d-dd24aa3250df",
                    "18013d13-0568-496b-b93b-2524c8132b93",
                    "id", // ?
                    "ETH_USDT",
                    1,
                    0.01,
                    0.01,
                    0.0001,
                    0.01,
                    "1",
                    OrderDirection.ASK,
                    MatchConstraint.GTC,
                    MatchingOrderType.LIMIT_ORDER,
                    100000.0,
                    0.01,
                    0.0, // ?
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC),
                    LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC)
                )
            )
        }
        val marketTradeResponses = marketQueryHandler.recentTrades("ETH_USDT", 10)

        assertThat(marketTradeResponses).isNotNull
        assertThat(marketTradeResponses.count()).isEqualTo(1)
        assertThat(marketTradeResponses.first()).isEqualTo(
            MarketTradeResponse(
                "ETH_USDT",
                1,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.001),
                BigDecimal.valueOf(0.001),
                Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
                true,
                true
            )
        )
    }
}

