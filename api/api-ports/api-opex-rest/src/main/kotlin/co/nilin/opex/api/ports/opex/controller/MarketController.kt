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
import kotlin.collections.mapNotNull
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

@RestController("opexMarketController")
@RequestMapping("/opex/v1/market")
@Tag(name = "Market", description = "Public market information, order book, trades, tickers, and basic market data.")
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val marketStatProxy: MarketStatProxy,
    private val marketDataProxy: MarketDataProxy,
    private val walletProxy: WalletProxy,
    private val matchingGatewayProxy: MatchingGatewayProxy,
    private val blockChainGatewayProxy: BlockchainGatewayProxy,
    @Value("\${app.user-activity-reference-currency}")
    private val userActivityReferenceCurrency: String
) {
    private val orderBookValidLimits = arrayListOf(5, 10, 20, 50, 100, 500, 1000, 5000)
    private val validDurations = arrayListOf("24h", "7d", "1M")

    @GetMapping("/currency")
    @Operation(
        summary = "Get currencies",
        description = """GET /opex/v1/market/currency.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CurrencyData::class)))])
        ]
    )
    suspend fun getCurrencies(): List<CurrencyData> {
        return walletProxy.getCurrencies()
    }

    @GetMapping("/pair")
    @Operation(
        summary = "Get pairs",
        description = """GET /opex/v1/market/pair.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = PairInfoResponse::class)))])
        ]
    )
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

    @GetMapping("/chain")
    @Operation(
        summary = "Get chains",
        description = """GET /opex/v1/market/chain.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = ChainInfo::class)))])
        ]
    )
    suspend fun getChains(): List<ChainInfo> {
        return blockChainGatewayProxy.getChainInfo()
    }

    @GetMapping("/currency/gateway")
    @Operation(
        summary = "Get currency gateways",
        description = """GET /opex/v1/market/currency/gateway.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CurrencyGatewayCommand::class)))])
        ]
    )
    suspend fun getCurrencyGateways(
        @RequestParam(defaultValue = "true") includeOffChainGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOnChainGateways: Boolean
    ): List<CurrencyGatewayCommand> {
        return walletProxy.getGateWays(includeOffChainGateways, includeOnChainGateways)
    }

    @GetMapping("/fee")
    @Operation(
        summary = "Get fee configs",
        description = """GET /opex/v1/market/fee.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = FeeConfig::class)))])
        ]
    )
    suspend fun getFeeConfigs(): List<FeeConfig> {
        return accountantProxy.getFeeConfigs()
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get market stats",
        description = """GET /opex/v1/market/stats.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = MarketInfoResponse::class))])
        ]
    )
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
            marketDataProxy.countTotalTrades(intervalEnum)
        )
    }

    @GetMapping("/depth")
    @Operation(
        summary = "Order book",
        description = """GET /opex/v1/market/depth.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = OrderBookResponse::class))])
        ]
    )
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
    @Operation(
        summary = "Recent trades",
        description = """GET /opex/v1/market/trades.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = RecentTradeResponse::class)))])
        ]
    )
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
    @Operation(
        summary = "Price change",
        description = """GET /opex/v1/market/ticker/{duration:24h|7d|1M}.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = PriceChange::class)))])
        ]
    )
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
    @Operation(
        summary = "Price ticker",
        description = """GET /opex/v1/market/ticker/price.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = PriceTicker::class)))])
        ]
    )
    suspend fun priceTicker(@RequestParam(required = false) symbol: String?): List<PriceTicker> {
        return marketDataProxy.lastPrice(symbol)
    }

    @GetMapping("/currencyInfo/quotes")
    @Operation(
        summary = "Get quote currencies",
        description = """GET /opex/v1/market/currencyInfo/quotes.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(type = "string")))])
        ]
    )
    suspend fun getQuoteCurrencies(): List<String> {
        return walletProxy.getQuoteCurrencies().map { it.currency }
    }

    @GetMapping("/klines")
    @Operation(
        summary = "Klines",
        description = """GET /opex/v1/market/klines.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = List<Any>::class)))])
        ]
    )
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
    @Operation(
        summary = "Get basic data",
        description = """GET /opex/v1/market/basic-data.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", schema = Schema(implementation = MarketBasicData::class))])
        ]
    )
    suspend fun getBasicData(): MarketBasicData {
        val quoteCurrencies = walletProxy.getQuoteCurrencies()
        return MarketBasicData(
            (quoteCurrencies.map { it.currency }),
            (quoteCurrencies.filter { it.isReference }.map { it.currency }),
            userActivityReferenceCurrency

        )
    }

    @GetMapping("/withdraw-limits")
    @Operation(
        summary = "Get withdraw limits",
        description = """GET /opex/v1/market/withdraw-limits.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = WithdrawLimitConfig::class)))])
        ]
    )
    suspend fun getWithdrawLimits(): List<WithdrawLimitConfig> {
        return accountantProxy.getWithdrawLimitConfigs()
    }

    @GetMapping("/gateway/{gatewayUuid}/terminal")
    @Operation(
        summary = "Get gateway terminal",
        description = """GET /opex/v1/market/gateway/{gatewayUuid}/terminal.
Security: Public endpoint. No Bearer token is required.""",
        responses = [
            ApiResponse(responseCode = "200", description = "Successful response.", content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = TerminalCommand::class)))])
        ]
    )
    suspend fun getGatewayTerminal(
        @PathVariable("gatewayUuid") gatewayUuid: String
    ): List<TerminalCommand>? {
        return walletProxy.getGatewayTerminal(gatewayUuid)
    }

    private fun getValidLimit(limit: Int?): Int = when {
        limit == null -> 100
        limit > 1000 -> 1000
        limit < 1 -> 1
        else -> limit
    }
}
