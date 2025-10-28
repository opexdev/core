package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.*
import co.nilin.opex.api.ports.opex.data.MarketInfoResponse
import co.nilin.opex.api.ports.opex.data.MarketStatResponse
import co.nilin.opex.api.ports.opex.data.OrderBookResponse
import co.nilin.opex.api.ports.opex.data.RecentTradeResponse
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.ZoneId

@RestController("opexMarketController")
@RequestMapping("/opex/v1/market")
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val marketStatProxy: MarketStatProxy,
    private val marketDataProxy: MarketDataProxy,
    private val walletProxy: WalletProxy,
    private val matchingGatewayProxy: MatchingGatewayProxy,
    @Value("\${app.trade-volume-calculation-currency}")
    private val tradeVolumeCalculationCurrency: String,
    @Value("\${app.withdraw-volume-calculation-currency}")
    private val withdrawVolumeCalculationCurrency: String
) {
    private val orderBookValidLimits = arrayListOf(5, 10, 20, 50, 100, 500, 1000, 5000)
    private val validDurations = arrayListOf("24h", "7d", "1M")

    @GetMapping("/currency")
    suspend fun getCurrencies(): List<CurrencyData> {
        return walletProxy.getCurrencies()
    }

    @GetMapping("/pair")
    suspend fun getPairs(): List<PairInfoResponse> {
        val pairSettings = matchingGatewayProxy.getPairSettings().associateBy { it.pair }

        return accountantProxy.getPairConfigs().mapNotNull { config ->
            pairSettings[config.pair]?.run {
                PairInfoResponse(
                    pair = config.pair,
                    baseAsset = config.leftSideWalletSymbol,
                    quoteAsset = config.rightSideWalletSymbol,
                    isAvailable = isAvailable,
                    minOrder = minOrder,
                    maxOrder = maxOrder,
                    orderTypes = orderTypes
                )
            }
        }
    }

    @GetMapping("/currency/gateway")
    suspend fun getCurrencyGateways(
        @RequestParam(defaultValue = "true") includeOffChainGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand> {
        return walletProxy.getGateWays(includeOffChainGateways, includeOnChainGateways)
    }

    @GetMapping("/pair/fee")
    suspend fun getPairFees(): List<FeeConfig> {
        return accountantProxy.getFeeConfigs()
    }

    @GetMapping("/stats")
    suspend fun getMarketStats(
        @RequestParam interval: String,
        @RequestParam(required = false) limit: Int?
    ): MarketStatResponse = coroutineScope {
        val intervalEnum = Interval.findByLabel(interval) ?: Interval.Week
        val validLimit = getValidLimit(limit)

        val mostIncreased = async {
            marketStatProxy.getMostIncreasedInPricePairs(intervalEnum, validLimit)
        }

        val mostDecreased = async {
            marketStatProxy.getMostDecreasedInPricePairs(intervalEnum, validLimit)
        }

        val highestVolume = async {
            marketStatProxy.getHighestVolumePair(intervalEnum)
        }

        val mostTrades = async {
            marketStatProxy.getTradeCountPair(intervalEnum)
        }

        MarketStatResponse(
            mostIncreased.await(),
            mostDecreased.await(),
            highestVolume.await(),
            mostTrades.await()
        )
    }

    @GetMapping("/info")
    suspend fun getMarketInfo(@RequestParam interval: String): MarketInfoResponse {
        val intervalEnum = Interval.findByLabel(interval) ?: Interval.ThreeMonth
        return MarketInfoResponse(
            marketDataProxy.countActiveUsers(intervalEnum),
            marketDataProxy.countTotalOrders(intervalEnum),
            marketDataProxy.countTotalTrades(intervalEnum),
        )
    }

    @GetMapping("/depth")
    suspend fun orderBook(
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        limit: Int? // Default 100; max 5000. Valid limits:[5, 10, 20, 50, 100, 500, 1000, 5000]
    ): OrderBookResponse {
        val validLimit = limit ?: 100
        if (!orderBookValidLimits.contains(validLimit))
            OpexError.InvalidLimitForOrderBook.exception()

        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketDataProxy.openBidOrders(symbol, validLimit)
        val askOrders = marketDataProxy.openAskOrders(symbol, validLimit)

        bidOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedBidOrders.add(mapped)
        }

        askOrders.forEach {
            val mapped = arrayListOf<BigDecimal>().apply {
                add(it.price ?: BigDecimal.ZERO)
                add(it.quantity ?: BigDecimal.ZERO)
            }
            mappedAskOrders.add(mapped)
        }

        val lastOrder = marketDataProxy.lastOrder(symbol)
        return OrderBookResponse(lastOrder?.orderId ?: -1, mappedBidOrders, mappedAskOrders)
    }

    @GetMapping("/trades")
    suspend fun recentTrades(
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        limit: Int? // Default 500; max 1000.
    ): List<RecentTradeResponse> {
        val validLimit = limit ?: 500
        if (validLimit !in 1..1000)
            OpexError.InvalidLimitForRecentTrades.exception()

        return marketDataProxy.recentTrades(symbol, validLimit)
            .map {
                RecentTradeResponse(
                    it.id,
                    it.price,
                    it.quantity,
                    it.quoteQuantity,
                    it.time.time,
                    it.isMakerBuyer,
                    it.isBestMatch
                )
            }
    }

    @GetMapping("/ticker/{duration:24h|7d|1M}")
    suspend fun priceChange(
        @PathVariable duration: String,
        @RequestParam(required = false) symbol: String?,
        @RequestParam(required = false) quote: String?
    ): List<PriceChange> {
        if (!validDurations.contains(duration))
            OpexError.InvalidPriceChangeDuration.exception()

        val interval = Interval.findByLabel(duration) ?: Interval.Week

        val result = if (symbol.isNullOrEmpty())
            marketDataProxy.getTradeTickerData(interval).toMutableList()
        else
            arrayListOf(marketDataProxy.getTradeTickerDataBySymbol(symbol, interval))

        result.forEach {
            val parts = it.symbol?.split("_")
            if (parts != null && parts.size == 2) {
                it.base = parts[0].uppercase()
                it.quote = parts[1].uppercase()
            }
        }

        return if (quote.isNullOrEmpty()) result else result.filter { it.quote.equals(quote, true) }
    }

    @GetMapping("/ticker/price")
    suspend fun priceTicker(@RequestParam(required = false) symbol: String?): List<PriceTicker> {
        return marketDataProxy.lastPrice(symbol)
    }

    @GetMapping("/currencyInfo/quotes")
    suspend fun getQuoteCurrencies(): List<String> {
        return walletProxy.getQuoteCurrencies().map { it.currency }
    }

    @GetMapping("/klines")
    suspend fun klines(
        @RequestParam
        symbol: String,
        @RequestParam
        interval: String,
        @RequestParam(required = false)
        startTime: Long?,
        @RequestParam(required = false)
        endTime: Long?,
        @RequestParam(required = false)
        limit: Int? // Default 500; max 1000.
    ): List<List<Any>> {
        val validLimit = limit ?: 500
        if (validLimit !in 1..1000)
            throw OpexError.InvalidLimitForRecentTrades.exception()

        val i = Interval.findByLabel(interval) ?: throw OpexError.InvalidInterval.exception()

        val list = ArrayList<ArrayList<Any>>()
        marketDataProxy.getCandleInfo(symbol, "${i.duration} ${i.unit}", startTime, endTime, validLimit)
            .forEach {
                list.add(
                    arrayListOf(
                        it.openTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        it.open.toString(),
                        it.high.toString(),
                        it.low.toString(),
                        it.close.toString(),
                        it.volume.toString(),
                        it.closeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        it.quoteAssetVolume.toString(),
                        it.trades,
                        it.takerBuyBaseAssetVolume.toString(),
                        it.takerBuyQuoteAssetVolume.toString(),
                        "0.0"
                    )
                )
            }
        return list
    }

    @GetMapping("/basic-data")
    suspend fun getBasicData(): MarketBasicData {
        val quoteCurrencies = walletProxy.getQuoteCurrencies()
        return MarketBasicData(
            (quoteCurrencies.map { it.currency }),
            (quoteCurrencies.filter { it.isReference }.map { it.currency }),
            withdrawVolumeCalculationCurrency,
            tradeVolumeCalculationCurrency
        )
    }

    @GetMapping("/withdraw-limits")
    suspend fun getWithdrawLimits(): List<WithdrawLimitConfig> {
        return accountantProxy.getWithdrawLimitConfigs()
    }

    private fun getValidLimit(limit: Int?): Int = when {
        limit == null -> 100
        limit > 1000 -> 1000
        limit < 1 -> 1
        else -> limit
    }
}