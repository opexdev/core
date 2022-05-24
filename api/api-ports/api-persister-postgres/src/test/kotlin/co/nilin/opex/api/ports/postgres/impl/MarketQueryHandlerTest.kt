package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.impl.sample.VALID
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MarketQueryHandlerTest {
    private val orderRepository: OrderRepository = mockk()
    private val tradeRepository: TradeRepository = mockk()
    private val orderStatusRepository: OrderStatusRepository = mockk()
    private val symbolMapper: SymbolMapper = mockk()
    private val marketQueryHandler =
        MarketQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository, symbolMapper)

    @Test
    fun givenAggregatedOrderPrice_whenOpenASKOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        every {
            orderRepository.findBySymbolAndDirectionAndStatusSortAscendingByPrice(
                eq(VALID.ETH_USDT),
                eq(OrderDirection.ASK),
                eq(1),
                match { it == listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code) }
            )
        } returns Flux.just(VALID.AGGREGATED_ORDER_PRICE_MODEL)

        val orderBookResponses = marketQueryHandler.openAskOrders(VALID.ETH_USDT, 1)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(VALID.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenAggregatedOrderPrice_whenOpenBIDOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        every {
            orderRepository.findBySymbolAndDirectionAndStatusSortDescendingByPrice(
                eq(VALID.ETH_USDT),
                eq(OrderDirection.BID),
                eq(1),
                match { it == listOf(OrderStatus.NEW.code, OrderStatus.PARTIALLY_FILLED.code) }
            )
        } returns Flux.just(VALID.AGGREGATED_ORDER_PRICE_MODEL)

        val orderBookResponses = marketQueryHandler.openBidOrders(VALID.ETH_USDT, 1)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(VALID.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenOrder_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        every {
            orderRepository.findLastOrderBySymbol(VALID.ETH_USDT)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderStatusRepository.findMostRecentByOUID(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)

        val queryOrderResponse = marketQueryHandler.lastOrder(VALID.ETH_USDT)

        assertThat(queryOrderResponse).isNotNull
        assertThat(queryOrderResponse).isEqualTo(VALID.MAKER_QUERY_ORDER_RESPONSE)
    }

    @Test
    fun givenOrderAndTradeAndSymbolAlias_whenLastPrice_thenPriceTickerResponse(): Unit = runBlocking {
        every {
            tradeRepository.findAllGroupBySymbol()
        } returns Flux.just(VALID.TRADE_MODEL)
        every {
            tradeRepository.findBySymbolGroupBySymbol(VALID.ETH_USDT)
        } returns Flux.just(VALID.TRADE_MODEL)
        every {
            orderRepository.findByOuid(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        coEvery {
            symbolMapper.map(VALID.ETH_USDT)
        } returns "ETHUSDT"

        val priceTickerResponse = marketQueryHandler.lastPrice(VALID.ETH_USDT)

        assertThat(priceTickerResponse).isNotNull
        assertThat(priceTickerResponse.size).isEqualTo(1)
        assertThat(priceTickerResponse.first().symbol).isEqualTo("ETHUSDT")
        assertThat(priceTickerResponse.first().price).isEqualTo("100000.0")
    }

    @Test
    fun givenOrderAndTrade_whenRecentTrades_thenMarketTradeResponseFlow(): Unit = runBlocking {
        every {
            tradeRepository.findBySymbolSortDescendingByCreateDate(VALID.ETH_USDT, 1)
        } returns flow {
            emit(VALID.TRADE_MODEL)
        }
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.makerOuid)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.takerOuid)
        } returns Mono.just(VALID.TAKER_ORDER_MODEL)

        val marketTradeResponses = marketQueryHandler.recentTrades(VALID.ETH_USDT, 1)

        assertThat(marketTradeResponses).isNotNull
        assertThat(marketTradeResponses.count()).isEqualTo(1)
        assertThat(marketTradeResponses.first()).isEqualTo(VALID.MARKET_TRADE_RESPONSE)
    }
}

