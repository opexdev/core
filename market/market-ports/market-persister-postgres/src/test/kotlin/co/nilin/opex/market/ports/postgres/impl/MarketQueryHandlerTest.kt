package co.nilin.opex.market.ports.postgres.impl

import co.nilin.opex.common.utils.Interval
import co.nilin.opex.market.core.inout.MarketTrade
import co.nilin.opex.market.core.inout.Order
import co.nilin.opex.market.core.inout.OrderDirection
import co.nilin.opex.market.core.inout.OrderStatus
import co.nilin.opex.market.ports.postgres.dao.OrderRepository
import co.nilin.opex.market.ports.postgres.dao.OrderStatusRepository
import co.nilin.opex.market.ports.postgres.dao.TradeRepository
import co.nilin.opex.market.ports.postgres.impl.sample.VALID
import co.nilin.opex.market.ports.postgres.model.CandleInfoData
import co.nilin.opex.market.ports.postgres.model.LastPrice
import co.nilin.opex.market.ports.postgres.model.TradeModel
import co.nilin.opex.market.ports.postgres.model.TradeTickerData
import co.nilin.opex.market.ports.postgres.util.RedisCacheHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

class MarketQueryHandlerTest {
    private val orderRepository = mockk<OrderRepository>()
    private val tradeRepository = mockk<TradeRepository>()
    private val orderStatusRepository = mockk<OrderStatusRepository>()
    private val redisCacheHelper = mockk<RedisCacheHelper>()
    private val marketQueryHandler =
        MarketQueryHandlerImpl(orderRepository, tradeRepository, orderStatusRepository, redisCacheHelper)

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
        assertThat(orderBookResponses?.size).isEqualTo(1)
        assertThat(orderBookResponses?.first()).isEqualTo(VALID.ORDER_BOOK_RESPONSE)
    }

    @Test
    fun givenOrder_whenLastOrder_thenReturnQueryOrderResponse(): Unit = runBlocking {
        every { redisCacheHelper.get<Order>(any()) } returns null
        every {
            orderRepository.findLastOrderBySymbol(VALID.ETH_USDT)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderStatusRepository.findMostRecentByOUID(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_STATUS_MODEL)
        every { redisCacheHelper.put(any(), any()) } returns Unit

        val queryOrderResponse = marketQueryHandler.lastOrder(VALID.ETH_USDT)

        assertThat(queryOrderResponse).isNotNull
        assertThat(queryOrderResponse).isEqualTo(VALID.MAKER_ORDER)
    }

    @Test
    fun givenOrderAndTradeAndSymbolAlias_whenLastPrice_thenPriceTickerResponse(): Unit = runBlocking {
        every {
            tradeRepository.findAllGroupBySymbol()
        } returns Flux.just(VALID.LAST_PRICE_MODEL)
        every {
            tradeRepository.findBySymbolGroupBySymbol(VALID.ETH_USDT)
        } returns Flux.just(VALID.LAST_PRICE_MODEL)
        every {
            orderRepository.findByOuid(VALID.MAKER_ORDER_MODEL.ouid)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        coEvery {
            redisCacheHelper.getOrElse<List<LastPrice>>(
                any(),
                any(),
                any()
            )
        } returns listOf(VALID.LAST_PRICE_MODEL)

        val priceTickerResponse = marketQueryHandler.lastPrice(VALID.ETH_USDT)

        assertThat(priceTickerResponse).isNotNull
        assertThat(priceTickerResponse.size).isEqualTo(1)
        assertThat(priceTickerResponse.first().symbol).isEqualTo("ETH_USDT")
        assertThat(priceTickerResponse.first().price).isEqualTo(VALID.TRADE_MODEL.let { it.makerPrice.min(it.takerPrice) }
            .toString())
    }

    @Test
    fun givenOrderAndTrade_whenRecentTrades_thenMarketTradeResponseFlow(): Unit = runBlocking {
        every { redisCacheHelper.getList<MarketTrade>(any()) } returns null
        every {
            tradeRepository.findBySymbolSortDescendingByCreateDate(VALID.ETH_USDT, 1)
        } returns Flux.just(VALID.TRADE_MODEL)
        every {
            tradeRepository.findRecentMarketTrades(VALID.ETH_USDT, 1)
        } returns Flux.just(VALID.MARKET_TRADE)
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.makerOuid)
        } returns Mono.just(VALID.MAKER_ORDER_MODEL)
        every {
            orderRepository.findByOuid(VALID.TRADE_MODEL.takerOuid)
        } returns Mono.just(VALID.TAKER_ORDER_MODEL)
        every { redisCacheHelper.putListItem(any(), any()) } returns Unit
        every { redisCacheHelper.setExpiration(any(), any()) } returns Unit

        val marketTradeResponses = marketQueryHandler.recentTrades(VALID.ETH_USDT, 1)

        assertThat(marketTradeResponses).isNotNull
        assertThat(marketTradeResponses?.count()).isEqualTo(1)
        assertThat(marketTradeResponses?.first()).isEqualTo(VALID.MARKET_TRADE_RESPONSE)
    }

    @Test
    fun givenTickerData_whenTradeTickerRequested_thenTickerTimeWindowIsOrderedCorrectly(): Unit = runBlocking {
        val tradeTickerData = TradeTickerData(
            VALID.ETH_USDT,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.TEN,
            BigDecimal.ONE,
            BigDecimal.TEN,
            1L,
            2L,
            3L
        )
        coEvery {
            redisCacheHelper.getOrElse<List<co.nilin.opex.market.core.inout.PriceChange>>(
                eq("tradeTickerData:${Interval.TwentyFourHours.label}"),
                any(),
                any()
            )
        } coAnswers {
            thirdArg<suspend () -> List<co.nilin.opex.market.core.inout.PriceChange>>().invoke()
        }
        every { tradeRepository.tradeTicker(any()) } returns Flux.just(tradeTickerData)

        val priceChanges = marketQueryHandler.getTradeTickerData(Interval.TwentyFourHours)

        assertThat(priceChanges).hasSize(1)
        assertThat(priceChanges.first().openTime).isLessThanOrEqualTo(priceChanges.first().closeTime)
    }

    @Test
    fun givenMissingCandleBounds_whenGetCandleInfo_thenOnlyLatestIntervalsAreRequested(): Unit = runBlocking {
        val latestTradeDate = LocalDateTime.of(2024, 1, 1, 10, 15)
        val expectedStartDate = latestTradeDate.minusHours(2)
        val latestTrade = TradeModel(
            1L,
            1L,
            VALID.ETH_USDT,
            "ETH",
            "USDT",
            BigDecimal.TEN,
            BigDecimal.ONE,
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "ETH",
            "USDT",
            latestTradeDate,
            "maker",
            "taker",
            "maker-user",
            "taker-user",
            latestTradeDate
        )
        val candleInfo = CandleInfoData(
            expectedStartDate,
            expectedStartDate.plusHours(1),
            BigDecimal.ONE,
            BigDecimal.TWO,
            BigDecimal.TWO,
            BigDecimal.ONE,
            BigDecimal.TEN,
            1
        )
        coEvery { tradeRepository.findLastByCreateDate() } returns Mono.just(latestTrade)
        coEvery {
            tradeRepository.candleData(
                VALID.ETH_USDT,
                "1 HOURS",
                expectedStartDate,
                latestTradeDate,
                3
            )
        } returns Flux.just(candleInfo)

        val candles = marketQueryHandler.getCandleInfo(VALID.ETH_USDT, "1 HOURS", null, null, 3)

        assertThat(candles).hasSize(1)
        assertThat(candles.first().openTime).isEqualTo(expectedStartDate)
        assertThat(candles.first().closeTime).isEqualTo(expectedStartDate.plusHours(1))
    }
}
