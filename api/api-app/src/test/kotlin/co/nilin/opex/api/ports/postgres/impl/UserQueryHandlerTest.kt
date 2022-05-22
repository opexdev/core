package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.*
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
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.assertj.core.api.Assertions.*
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stubbing
import reactor.core.publisher.Mono
import java.math.BigDecimal

class UserQueryHandlerTest {
    private val orderRepository: OrderRepository = mock()
    private val tradeRepository: TradeRepository = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val userQueryHandler = UserQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository)
    private val principal: Principal = Principal { "98c7ca9b-2d9c-46dd-afa8-b0cd2f52a97c" }

    @Test
    fun given_whenAllOrders_then(): Unit = runBlocking {
        val allOrderRequest = AllOrderRequest(
            "ETH_USDT",
            Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
            Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
            500
        )
        stubbing(orderRepository) {
            on {
                findByUuidAndSymbolAndTimeBetween(
                    principal.name,
                    allOrderRequest.symbol,
                    allOrderRequest.startTime,
                    allOrderRequest.endTime
                )
            } doReturn flow {
                emit(
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
        }

        val queryOrderResponses = userQueryHandler.allOrders(principal, allOrderRequest)

        assertThat(queryOrderResponses).isNotNull
        assertThat(queryOrderResponses.count()).isEqualTo(1)
        assertThat(queryOrderResponses.first()).isEqualTo(
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
    fun given_whenAllTrades_then(): Unit = runBlocking {
        val tradeRequest = TradeRequest(
            "ETH_USDT",
            1,
            Date.from(LocalDateTime.ofEpochSecond(1653125640, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
            Date.from(LocalDateTime.ofEpochSecond(1653125840, 0, ZoneOffset.UTC).toInstant(ZoneOffset.UTC)),
            500
        )
        stubbing(tradeRepository) {
            on {
                findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
                    principal.name,
                    tradeRequest.symbol,
                    1,
                    tradeRequest.startTime,
                    tradeRequest.endTime
                )
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

        val tradeResponses = userQueryHandler.allTrades(principal, tradeRequest)

        assertThat(tradeResponses).isNotNull
        assertThat(tradeResponses.count()).isEqualTo(1)
    }

    @Test
    fun given_whenOpenOrders_then(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findByUuidAndSymbolAndStatus(principal.name, "ETH_USDT", listOf(0))
            } doReturn flow {
                emit(
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
        }

        val queryOrderResponses = userQueryHandler.openOrders(principal, "ETH_USDT")

        assertThat(queryOrderResponses).isNotNull
        assertThat(queryOrderResponses.count()).isEqualTo(1)
    }

    @Test
    fun given_whenQueryOrder_then(): Unit = runBlocking {
        val queryOrderRequest = QueryOrderRequest(
            "ETH_USDT",
            1,
            "2" // ?
        )
        stubbing(orderRepository) {
            on {
                findBySymbolAndClientOrderId("ETH_USDT", "id")
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
                findBySymbolAndOrderId("ETH_USDT", 1)
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

        val queryOrderResponse = userQueryHandler.queryOrder(principal, queryOrderRequest)
        assertThat(queryOrderResponse).isNotNull
    }
}
