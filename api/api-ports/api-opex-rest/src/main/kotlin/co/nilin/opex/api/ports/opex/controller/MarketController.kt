package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.*
import co.nilin.opex.api.ports.opex.data.MarketInfoResponse
import co.nilin.opex.api.ports.opex.data.MarketStatResponse
import co.nilin.opex.api.ports.opex.data.OrderBookResponse
import co.nilin.opex.api.ports.opex.data.RecentTradeResponse
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.ZoneId

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
        description = """
Security:
- Public endpoint. No Bearer token is required.

Response body:
- Array of currency data.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Currencies returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = CurrencyData::class))
                    )
                ]
            )
        ]
    )
    suspend fun getCurrencies(): List<CurrencyData> {
        return walletProxy.getCurrencies()
    }

    @GetMapping("/pair")
    @Operation(
        summary = "Get trading pairs",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Response body:
- Array of pair info.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Trading pairs returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = PairInfoResponse::class))
                    )
                ]
            )
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
        description = """
Security:
- Public endpoint. No Bearer token is required.

Source of values:
- Chain names returned here should be used by clients when selecting chain-based fields in other APIs.

Response body:
- Array of chain info.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Chains returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = ChainInfo::class))
                    )
                ]
            )
        ]
    )
    suspend fun getChains(): List<ChainInfo> {
        return blockChainGatewayProxy.getChainInfo()
    }

    @GetMapping("/currency/gateway")
    @Operation(
        summary = "Get currency gateways",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Behavior:
- `includeOffChainGateways` defaults to `true`.
- `includeOnChainGateways` defaults to `true`.
- Set either flag to `false` to exclude that gateway type.

Response body:
- Array of currency gateway commands.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Currency gateways returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = CurrencyGatewayCommand::class))
                    )
                ]
            )
        ]
    )
    suspend fun getCurrencyGateways(
        @Parameter(
            name = "includeOffChainGateways",
            description = "Whether OffChain gateways should be included. Defaults to true.",
            required = false,
            schema = Schema(type = "boolean", defaultValue = "true")
        )
        @RequestParam(name = "includeOffChainGateways", defaultValue = "true")
        includeOffChainGateways: Boolean,

        @Parameter(
            name = "includeOnChainGateways",
            description = "Whether OnChain gateways should be included. Defaults to true.",
            required = false,
            schema = Schema(type = "boolean", defaultValue = "true")
        )
        @RequestParam(name = "includeOnChainGateways", defaultValue = "true")
        includeOnChainGateways: Boolean
    ): List<CurrencyGatewayCommand> {
        return walletProxy.getGateWays(includeOffChainGateways, includeOnChainGateways)
    }

    @GetMapping("/fee")
    @Operation(
        summary = "Get fee configs",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Response body:
- Array of fee config data.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Fee configs returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = FeeConfig::class))
                    )
                ]
            )
        ]
    )
    suspend fun getFeeConfigs(): List<FeeConfig> {
        return accountantProxy.getFeeConfigs()
    }

    @GetMapping("/stats")
    @Operation(
        summary = "Get market stats",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Behavior:
- `interval` uses interval labels such as `1d`, `1w`, `1M`, or `3M`.
- If `interval` is invalid, the service falls back to `1w`.
- `limit` defaults to `100`.
- `limit` is clamped to the range `1..1000`.

Response body:
- Market statistics response.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Market stats returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MarketStatResponse::class)
                    )
                ]
            )
        ]
    )
    suspend fun getMarketStats(
        @Parameter(
            name = "interval",
            description = "Interval label. Invalid values fall back to 1w.",
            required = true,
            schema = Schema(
                type = "string",
                allowableValues = [
                    "1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "6h", "8h", "12h",
                    "24h", "1d", "3d", "7d", "1w", "1M", "3M", "1Y"
                ]
            ),
            example = "1w"
        )
        @RequestParam(name = "interval")
        interval: String,

        @Parameter(
            name = "limit",
            description = "Optional result limit. Defaults to 100 and is clamped to 1..1000.",
            required = false,
            schema = Schema(type = "integer", format = "int32", defaultValue = "100"),
            example = "100"
        )
        @RequestParam(name = "limit", required = false)
        limit: Int?
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
    @Operation(
        summary = "Get market info",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Behavior:
- `interval` uses interval labels such as `1d`, `1w`, `1M`, or `3M`.
- If `interval` is invalid, the service falls back to `3M`.

Response body:
- Market info response.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Market info returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MarketInfoResponse::class)
                    )
                ]
            )
        ]
    )
    suspend fun getMarketInfo(
        @Parameter(
            name = "interval",
            description = "Interval label. Invalid values fall back to 3M.",
            required = true,
            schema = Schema(
                type = "string",
                allowableValues = [
                    "1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "6h", "8h", "12h",
                    "24h", "1d", "3d", "7d", "1w", "1M", "3M", "1Y"
                ]
            ),
            example = "3M"
        )
        @RequestParam(name = "interval")
        interval: String
    ): MarketInfoResponse {
        val intervalEnum = Interval.findByLabel(interval) ?: Interval.ThreeMonth
        return MarketInfoResponse(
            marketDataProxy.countActiveUsers(intervalEnum),
            marketDataProxy.countTotalOrders(intervalEnum),
            marketDataProxy.countTotalTrades(intervalEnum)
        )
    }

    @GetMapping("/depth")
    @Operation(
        summary = "Get order book",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Validation:
- `limit` must be one of: `5`, `10`, `20`, `50`, `100`, `500`, `1000`, `5000`.

Behavior:
- `limit` defaults to `100` when omitted.

Response body:
- Order book response.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Order book returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OrderBookResponse::class)
                    )
                ]
            )
        ]
    )
    suspend fun orderBook(
        @Parameter(
            name = "symbol",
            description = "Market symbol.",
            required = true,
            example = "BTCUSDT"
        )
        @RequestParam(name = "symbol")
        symbol: String,

        @Parameter(
            name = "limit",
            description = "Optional order book limit. Defaults to 100.",
            required = false,
            schema = Schema(
                type = "integer",
                format = "int32",
                defaultValue = "100",
                allowableValues = ["5", "10", "20", "50", "100", "500", "1000", "5000"]
            ),
            example = "100"
        )
        @RequestParam(name = "limit", required = false)
        limit: Int?
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
        summary = "Get recent trades",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Validation:
- `limit` must be between `1` and `1000`.

Behavior:
- `limit` defaults to `500` when omitted.

Response body:
- Array of recent trades.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Recent trades returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = RecentTradeResponse::class))
                    )
                ]
            )
        ]
    )
    suspend fun recentTrades(
        @Parameter(
            name = "symbol",
            description = "Market symbol.",
            required = true,
            example = "BTCUSDT"
        )
        @RequestParam(name = "symbol")
        symbol: String,

        @Parameter(
            name = "limit",
            description = "Optional recent-trade limit. Defaults to 500. Valid range: 1..1000.",
            required = false,
            schema = Schema(type = "integer", format = "int32", defaultValue = "500", minimum = "1", maximum = "1000"),
            example = "500"
        )
        @RequestParam(name = "limit", required = false)
        limit: Int?
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
        summary = "Get price change tickers",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Validation:
- `duration` must be one of: `24h`, `7d`, `1M`.

Behavior:
- If `symbol` is omitted, all symbols are returned.
- If `quote` is provided, results are filtered by quote currency.

Source of values:
- Quote values should come from `/opex/v1/market/currencyInfo/quotes`.

Response body:
- Array of price change data.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Price changes returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = PriceChange::class))
                    )
                ]
            )
        ]
    )
    suspend fun priceChange(
        @Parameter(
            name = "duration",
            description = "Ticker duration.",
            required = true,
            schema = Schema(type = "string", allowableValues = ["24h", "7d", "1M"]),
            example = "24h"
        )
        @PathVariable("duration")
        duration: String,

        @Parameter(
            name = "symbol",
            description = "Optional market symbol. If omitted, all symbols are returned.",
            required = false,
            example = "BTCUSDT"
        )
        @RequestParam(name = "symbol", required = false)
        symbol: String?,

        @Parameter(
            name = "quote",
            description = "Optional quote currency filter.",
            required = false,
            example = "USDT"
        )
        @RequestParam(name = "quote", required = false)
        quote: String?
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
        summary = "Get price ticker",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Behavior:
- If `symbol` is omitted, prices for all available symbols are returned.

Response body:
- Array of price ticker data.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Price ticker returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = PriceTicker::class))
                    )
                ]
            )
        ]
    )
    suspend fun priceTicker(
        @Parameter(
            name = "symbol",
            description = "Optional market symbol. If omitted, all available symbols are returned.",
            required = false,
            example = "BTCUSDT"
        )
        @RequestParam(name = "symbol", required = false)
        symbol: String?
    ): List<PriceTicker> {
        return marketDataProxy.lastPrice(symbol)
    }

    @GetMapping("/currencyInfo/quotes")
    @Operation(
        summary = "Get quote currencies",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Source of values:
- Returned values can be used as the `quote` filter in ticker endpoints.

Response body:
- Array of quote currency strings.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Quote currencies returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(type = "string"))
                    )
                ]
            )
        ]
    )
    suspend fun getQuoteCurrencies(): List<String> {
        return walletProxy.getQuoteCurrencies().map { it.currency }
    }

    @GetMapping("/klines")
    @Operation(
        summary = "Get klines",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Validation:
- `interval` must match one of the supported interval labels.
- `limit` must be between `1` and `1000`.

Behavior:
- `limit` defaults to `500` when omitted.
- `startTime` and `endTime` are Unix timestamps in milliseconds.
- Response rows are nested arrays in candlestick order.

Response body:
- Nested array of kline rows.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Klines returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(type = "array"))
                    )
                ]
            )
        ]
    )
    suspend fun klines(
        @Parameter(
            name = "symbol",
            description = "Market symbol.",
            required = true,
            example = "BTCUSDT"
        )
        @RequestParam(name = "symbol")
        symbol: String,

        @Parameter(
            name = "interval",
            description = "Candlestick interval label.",
            required = true,
            schema = Schema(
                type = "string",
                allowableValues = [
                    "1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "6h", "8h", "12h",
                    "24h", "1d", "3d", "7d", "1w", "1M", "3M", "1Y"
                ]
            ),
            example = "1h"
        )
        @RequestParam(name = "interval")
        interval: String,

        @Parameter(
            name = "startTime",
            description = "Optional start time as Unix timestamp in milliseconds.",
            required = false,
            schema = Schema(type = "integer", format = "int64")
        )
        @RequestParam(name = "startTime", required = false)
        startTime: Long?,

        @Parameter(
            name = "endTime",
            description = "Optional end time as Unix timestamp in milliseconds.",
            required = false,
            schema = Schema(type = "integer", format = "int64")
        )
        @RequestParam(name = "endTime", required = false)
        endTime: Long?,

        @Parameter(
            name = "limit",
            description = "Optional kline limit. Defaults to 500. Valid range: 1..1000.",
            required = false,
            schema = Schema(type = "integer", format = "int32", defaultValue = "500", minimum = "1", maximum = "1000"),
            example = "500"
        )
        @RequestParam(name = "limit", required = false)
        limit: Int?
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
        summary = "Get basic market data",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Response body:
- Basic market data.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Basic market data returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MarketBasicData::class)
                    )
                ]
            )
        ]
    )
    suspend fun getBasicData(): MarketBasicData {
        val quoteCurrencies = walletProxy.getQuoteCurrencies()
        return MarketBasicData(
            quoteCurrencies.map { it.currency },
            quoteCurrencies.filter { it.isReference }.map { it.currency },
            userActivityReferenceCurrency
        )
    }

    @GetMapping("/withdraw-limits")
    @Operation(
        summary = "Get withdraw limits",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Response body:
- Array of withdraw limit configs.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Withdraw limits returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = WithdrawLimitConfig::class))
                    )
                ]
            )
        ]
    )
    suspend fun getWithdrawLimits(): List<WithdrawLimitConfig> {
        return accountantProxy.getWithdrawLimitConfigs()
    }

    @GetMapping("/gateway/{gatewayUuid}/terminal")
    @Operation(
        summary = "Get gateway terminals",
        description = """
Security:
- Public endpoint. No Bearer token is required.

Source of values:
- `gatewayUuid` should come from currency gateway data.

Response body:
- Array of terminal commands, or null if no data is returned.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Gateway terminals returned successfully.",
                content = [
                    Content(
                        mediaType = "application/json",
                        array = ArraySchema(schema = Schema(implementation = TerminalCommand::class))
                    )
                ]
            )
        ]
    )
    suspend fun getGatewayTerminal(
        @Parameter(
            name = "gatewayUuid",
            description = "Gateway UUID.",
            required = true,
            example = "ofg-uuid-sample"
        )
        @PathVariable("gatewayUuid")
        gatewayUuid: String
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
