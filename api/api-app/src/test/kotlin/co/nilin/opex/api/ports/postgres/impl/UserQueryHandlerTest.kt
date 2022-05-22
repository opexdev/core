package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import org.assertj.core.api.Assertions.*
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

        val tradeResponses = userQueryHandler.allTrades(principal, tradeRequest)

        assertThat(tradeResponses).isNotNull
        assertThat(tradeResponses.count()).isEqualTo(1)
    }

    @Test
    fun given_whenOpenOrders_then(): Unit = runBlocking {
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

        val queryOrderResponse = userQueryHandler.queryOrder(principal, queryOrderRequest)
        assertThat(queryOrderResponse).isNotNull
    }
}
