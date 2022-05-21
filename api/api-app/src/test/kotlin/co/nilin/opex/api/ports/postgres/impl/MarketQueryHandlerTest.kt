package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.OrderRepository
import co.nilin.opex.api.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.api.ports.postgres.dao.TradeRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.security.Principal

class MarketQueryHandlerTest {
    private val orderRepository: OrderRepository = mock()
    private val tradeRepository: TradeRepository = mock()
    private val orderStatusRepository: OrderStatusRepository = mock()
    private val symbolMapper: SymbolMapper = mock()
    private val marketQueryHandler =
        MarketQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository, symbolMapper)

    @Test
    fun givenSymbol_whenOpenASKOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        marketQueryHandler.openAskOrders("ETH_USDT", 10)
    }

    @Test
    fun givenSymbol_whenOpenBIDOrders_thenReturnOrderBookResponseList(): Unit = runBlocking {
        marketQueryHandler.openBidOrders("ETH_USDT", 10)
    }

    @Test
    fun givenSymbol_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        marketQueryHandler.lastOrder("ETH_USDT")
    }

    @Test
    fun givenSymbol_whenLastPrice_thenPriceTickerResponse(): Unit = runBlocking {
        marketQueryHandler.lastPrice("ETH_USDT")
    }

    @Test
    fun givenSymbol_whenRecentTrades_thenMarketTradeResponseFlow(): Unit = runBlocking {
        marketQueryHandler.recentTrades("ETH_USDT", 10)
    }
}

