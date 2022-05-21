package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.AllOrderRequest
import co.nilin.opex.api.core.inout.QueryOrderRequest
import co.nilin.opex.api.core.inout.TradeRequest
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

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

        userQueryHandler.allOrders(principal, allOrderRequest)
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

        userQueryHandler.allTrades(principal, tradeRequest)
    }

    @Test
    fun given_whenOpenOrders_then(): Unit = runBlocking {
        userQueryHandler.openOrders(principal, "ETH_USDT")
    }

    @Test
    fun given_whenQueryOrder_then(): Unit = runBlocking {
        val queryOrderRequest = QueryOrderRequest(
            "ETH_USDT",
            1,
            "2" // ?
        )

        userQueryHandler.queryOrder(principal, queryOrderRequest)
    }
}
