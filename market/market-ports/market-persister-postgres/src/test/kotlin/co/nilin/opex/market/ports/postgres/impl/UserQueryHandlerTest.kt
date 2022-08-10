package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class UserQueryHandlerTest {
    private val orderRepository: OrderRepository = mockk()
    private val tradeRepository: TradeRepository = mockk()
    private val orderStatusRepository: OrderStatusRepository = mockk()
    private val userQueryHandler = UserQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository)

    @Test
    fun givenOrder_whenAllOrders_thenReturnQueryOrderResponseList(): Unit = runBlocking {
        every {
            orderRepository.findByUuidAndSymbolAndTimeBetween(
                VALID.PRINCIPAL.name,
                VALID.ALL_ORDER_REQUEST.symbol,
                VALID.ALL_ORDER_REQUEST.startTime,
                VALID.ALL_ORDER_REQUEST.endTime
            )
        } returns flow {
            emit(VALID.MAKER_ORDER_MODEL)
        }
        every {
            orderStatusRepository.findMostRecentByOUID(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        val queryOrderResponses = userQueryHandler.allOrders(VALID.PRINCIPAL.name, VALID.ALL_ORDER_REQUEST)

        assertThat(queryOrderResponses).isNotNull
        assertThat(queryOrderResponses.count()).isEqualTo(1)
        assertThat(queryOrderResponses.first()).isEqualTo(VALID.MAKER_ORDER)
    }

    @Test
    fun givenOrderAndTrade_whenAllTrades_thenTradeResponseList(): Unit = runBlocking {
        every {
            tradeRepository.findByUuidAndSymbolAndTimeBetweenAndTradeIdGreaterThan(
                VALID.PRINCIPAL.name,
                VALID.TRADE_REQUEST.symbol,
                1,
                VALID.TRADE_REQUEST.startTime,
                VALID.TRADE_REQUEST.endTime
            )
        } returns flow {
            emit(VALID.TRADE_MODEL)
        }
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.makerOuid)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.takerOuid)
        } returns Mono.just(VALID.TAKER_ORDER_MODEL)

        val tradeResponses = userQueryHandler.allTrades(VALID.PRINCIPAL.name, VALID.TRADE_REQUEST)

        assertThat(tradeResponses).isNotNull
        assertThat(tradeResponses.count()).isEqualTo(1)
    }

    @Test
    fun givenOrder_whenOpenOrders_thenReturnQueryOrderResponseList(): Unit = runBlocking {
        every {
            orderRepository.findByUuidAndSymbolAndStatus(
                eq(VALID.PRINCIPAL.name),
                eq(VALID.ETH_USDT),
                arrayListOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code)
            )
        } returns flow {
            emit(VALID.MAKER_ORDER_MODEL)
        }
        every {
            orderStatusRepository.findMostRecentByOUID(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        val queryOrderResponses = userQueryHandler.openOrders(VALID.PRINCIPAL.name, VALID.ETH_USDT)

        assertThat(queryOrderResponses).isNotNull
        assertThat(queryOrderResponses.count()).isEqualTo(1)
    }

    @Test
    fun givenOrder_whenQueryOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        every {
            orderRepository.findBySymbolAndClientOrderId(VALID.ETH_USDT, "2")
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderRepository.findBySymbolAndOrderId(VALID.ETH_USDT, VALID.MAKER_ORDER_MODEL.orderId!!)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderStatusRepository.findMostRecentByOUID(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        val queryOrderResponse = userQueryHandler.queryOrder(VALID.PRINCIPAL.name, VALID.QUERY_ORDER_REQUEST)

        assertThat(queryOrderResponse).isNotNull
    }
}
