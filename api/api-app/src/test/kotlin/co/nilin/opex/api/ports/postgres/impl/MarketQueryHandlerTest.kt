package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.OrderDirection
import co.nilin.opex.api.core.inout.OrderStatus
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import co.nilin.opex.api.ports.postgres.impl.testfixtures.Valid
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
                findBySymbolAndDirectionAndStatusSortAscendingByPrice(
                    eq("ETH_USDT"),
                    eq(OrderDirection.ASK),
                    eq(1),
                    argThat {
                        this == listOf(
                            OrderStatus.NEW.code,
                            OrderStatus.PARTIALLY_FILLED.code
                        )
                    }
                )
            } doReturn Flux.just(Valid.AGGREGATED_ORDER_PRICE_MODEL)
        }
        val orderBookResponses = marketQueryHandler.openAskOrders("ETH_USDT", 1)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(Valid.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenSymbol_whenOpenBIDOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findBySymbolAndDirectionAndStatusSortDescendingByPrice(
                    eq("ETH_USDT"),
                    eq(OrderDirection.BID),
                    eq(1),
                    argThat {
                        this == listOf(
                            OrderStatus.NEW.code,
                            OrderStatus.PARTIALLY_FILLED.code
                        )
                    }
                )
            } doReturn Flux.just(Valid.AGGREGATED_ORDER_PRICE_MODEL)
        }

        val orderBookResponses = marketQueryHandler.openBidOrders("ETH_USDT", 1)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(Valid.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenSymbol_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findLastOrderBySymbol("ETH_USDT")
            } doReturn Mono.just(Valid.MAKER_ORDER_MODEL)
        }
        stubbing(orderStatusRepository) {
            on {
                findMostRecentByOUID("f1167d30-ccc0-4f86-ab5d-dd24aa3250df")
            } doReturn Mono.just(Valid.ORDER_STATUS_MODEL)
        }

        val queryOrderResponse = marketQueryHandler.lastOrder("ETH_USDT")

        assertThat(queryOrderResponse).isNotNull
        assertThat(queryOrderResponse).isEqualTo(Valid.QUERY_ORDER_RESPONSE)
    }

    @Test
    fun givenSymbol_whenLastPrice_thenPriceTickerResponse(): Unit = runBlocking {
        stubbing(tradeRepository) {
            on {
                findAllGroupBySymbol()
            } doReturn Flux.just(Valid.TRADE_MODEL)
            on {
                findBySymbolGroupBySymbol("ETH_USDT")
            } doReturn Flux.just(Valid.TRADE_MODEL)
        }
        stubbing(orderRepository) {
            on {
                findByOuid(Valid.MAKER_ORDER_MODEL.ouid)
            } doReturn Mono.just(Valid.MAKER_ORDER_MODEL)
        }
        stubbing(symbolMapper) {
            onBlocking {
                map("ETH_USDT")
            } doReturn "ETHUSDT"
        }
        val priceTickerResponse = marketQueryHandler.lastPrice("ETH_USDT")

        assertThat(priceTickerResponse).isNotNull
        assertThat(priceTickerResponse.size).isEqualTo(1)
        assertThat(priceTickerResponse.first().symbol).isEqualTo("ETHUSDT")
        assertThat(priceTickerResponse.first().price).isEqualTo(100000)
    }

    @Test
    fun givenSymbol_whenRecentTrades_thenMarketTradeResponseFlow(): Unit = runBlocking {
        stubbing(tradeRepository) {
            on {
                findBySymbolSortDescendingByCreateDate("ETH_USDT", 1)
            } doReturn flow {
                emit(Valid.TRADE_MODEL)
            }
        }
        stubbing(orderRepository) {
            on {
                findByOuid(Valid.TRADE_MODEL.makerOuid)
            } doReturn Mono.just(Valid.MAKER_ORDER_MODEL)
            on {
                findByOuid(Valid.TRADE_MODEL.takerOuid)
            } doReturn Mono.just(Valid.TAKER_ORDER_MODEL)
        }

        val marketTradeResponses = marketQueryHandler.recentTrades("ETH_USDT", 1)

        assertThat(marketTradeResponses).isNotNull
        assertThat(marketTradeResponses.count()).isEqualTo(1)
        assertThat(marketTradeResponses.first()).isEqualTo(Valid.MARKET_TRADE_RESPONSE)
    }
}

