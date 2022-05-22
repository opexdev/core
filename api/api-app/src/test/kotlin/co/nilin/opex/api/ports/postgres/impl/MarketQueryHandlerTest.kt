package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.inout.OrderDirection
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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
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
                findBySymbolAndDirectionAndStatusSortAscendingByPrice("ETH_USDT", OrderDirection.BID, 1, listOf(0))
            } doReturn Flux.just(Valid.AGGREGATED_ORDER_PRICE_MODEL)
        }
        val orderBookResponses = marketQueryHandler.openAskOrders("ETH_USDT", 10)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(Valid.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenSymbol_whenOpenBIDOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findBySymbolAndDirectionAndStatusSortDescendingByPrice("ETH_USDT", OrderDirection.BID, 1, listOf(0))
            } doReturn Flux.just(Valid.AGGREGATED_ORDER_PRICE_MODEL)
        }

        val orderBookResponses = marketQueryHandler.openBidOrders("ETH_USDT", 10)

        assertThat(orderBookResponses).isNotNull
        assertThat(orderBookResponses.size).isEqualTo(1)
        assertThat(orderBookResponses.first()).isEqualTo(Valid.ORDER_STATUS_MODEL)
    }

    @Test
    fun givenSymbol_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        stubbing(orderRepository) {
            on {
                findLastOrderBySymbol("ETH_USDT")
            } doReturn Mono.just(Valid.ORDER_MODEL)
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
                findByOuid("99289106-2775-44d4-bffc-ca35fc25e58c")
            } doReturn Mono.just(Valid.ORDER_MODEL)
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
                emit(Valid.TRADE_MODEL)
            }
        }
        stubbing(orderRepository) {
            on {
                findByOuid("99289106-2775-44d4-bffc-ca35fc25e58c")
            } doReturn Mono.just(Valid.ORDER_MODEL)
            on {
                findByOuid("2fa73fa2-6d70-44b8-8571-e2b24e2eea2b")
            } doReturn Mono.just(Valid.ORDER_MODEL)
        }

        val marketTradeResponses = marketQueryHandler.recentTrades("ETH_USDT", 10)

        assertThat(marketTradeResponses).isNotNull
        assertThat(marketTradeResponses.count()).isEqualTo(1)
        assertThat(marketTradeResponses.first()).isEqualTo(Valid.MARKET_TRADE_RESPONSE)
    }
}

