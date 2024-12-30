package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.inout.PriceChange
import co.nilin.opex.api.core.inout.PriceTicker
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.binance.data.*
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.Interval
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.security.Principal
import java.time.ZoneId

@RestController
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val marketDataProxy: MarketDataProxy,
    private val blockchainGatewayProxy: BlockchainGatewayProxy,
    private val symbolMapper: SymbolMapper
) {

    private val orderBookValidLimits = arrayListOf(5, 10, 20, 50, 100, 500, 1000, 5000)
    private val validDurations = arrayListOf("24h", "7d", "1M")

    // Limit - Weight
    // 5, 10, 20, 50, 100 - 1
    // 500 - 5
    // 1000 - 10
    // 5000 - 50
    @GetMapping("/v3/depth")
    suspend fun orderBook(
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        limit: Int? // Default 100; max 5000. Valid limits:[5, 10, 20, 50, 100, 500, 1000, 5000]
    ): OrderBookResponse {
        val validLimit = limit ?: 100
        val localSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        if (!orderBookValidLimits.contains(validLimit))
            OpexError.InvalidLimitForOrderBook.exception()

        val mappedBidOrders = ArrayList<ArrayList<BigDecimal>>()
        val mappedAskOrders = ArrayList<ArrayList<BigDecimal>>()

        val bidOrders = marketDataProxy.openBidOrders(localSymbol, validLimit)
        val askOrders = marketDataProxy.openAskOrders(localSymbol, validLimit)

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

        val lastOrder = marketDataProxy.lastOrder(localSymbol)
        return OrderBookResponse(lastOrder?.orderId ?: -1, mappedBidOrders, mappedAskOrders)
    }

    @GetMapping("/v3/trades")
    suspend fun recentTrades(
        principal: Principal,
        @RequestParam
        symbol: String,
        @RequestParam(required = false)
        limit: Int? // Default 500; max 1000.
    ): List<RecentTradeResponse> {
        val validLimit = limit ?: 500
        val localSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        if (validLimit !in 1..1000)
            OpexError.InvalidLimitForRecentTrades.exception()

        return marketDataProxy.recentTrades(localSymbol, validLimit)
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

    @GetMapping("/v3/ticker/{duration:24h|7d|1M}")
    suspend fun priceChange(
        @PathVariable duration: String,
        @RequestParam(required = false) symbol: String?,
        @RequestParam(required = false) quote: String?
    ): List<PriceChange> {
        val localSymbol = if (symbol.isNullOrEmpty())
            null
        else
            symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()

        if (!validDurations.contains(duration))
            OpexError.InvalidPriceChangeDuration.exception()

        val interval = Interval.findByLabel(duration) ?: Interval.Week

        val result = if (symbol.isNullOrEmpty())
            marketDataProxy.getTradeTickerData(interval).toMutableList()
        else
            arrayListOf(marketDataProxy.getTradeTickerDataBySymbol(localSymbol!!, interval))

        symbolMapper.symbolToAliasMap().entries.forEach { map ->
            val price = result.find { it.symbol == map.key }
            val symbolBase = map.key.split("_")[0].uppercase()
            val symbolQuote = map.key.split("_")[1].uppercase()

            if (price == null && symbol.isNullOrEmpty())
                result.add(PriceChange(map.value, symbolBase, symbolQuote))
            else {
                price?.symbol = map.value
                price?.base = symbolBase
                price?.quote = symbolQuote
            }
        }

        return if (quote.isNullOrEmpty()) result else result.filter { it.quote.equals(quote, true) }
    }

    // Weight
    // 1 for a single symbol
    // 2 when the symbol parameter is omitted
    @GetMapping("/v3/ticker/price")
    suspend fun priceTicker(@RequestParam(required = false) symbol: String?): List<PriceTicker> {
        val symbols = symbolMapper.symbolToAliasMap()
        val localSymbol = if (symbol == null)
            null
        else
            symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        return marketDataProxy.lastPrice(localSymbol).onEach { symbols[it.symbol]?.let { s -> it.symbol = s } }
    }

    @GetMapping("/v3/exchangeInfo")
    suspend fun pairInfo(
        @RequestParam(required = false)
        symbol: String?,
        @RequestParam(required = false)
        symbols: String?
    ): ExchangeInfoResponse = coroutineScope {
        val symbolsMap = symbolMapper.symbolToAliasMap()
        val fee = async { accountantProxy.getFeeConfigs() }
        val pairConfigs = async {
            accountantProxy.getPairConfigs().map {
                ExchangeInfoSymbol(
                    symbolsMap[it.pair] ?: it.pair,
                    "TRADING",
                    it.leftSideWalletSymbol.uppercase(),
                    it.leftSideFraction.scale() - it.leftSideFraction.precision() + 1,
                    it.rightSideWalletSymbol.uppercase(),
                    it.rightSideFraction.scale()- it.rightSideFraction.precision() + 1
                )
            }
        }
        ExchangeInfoResponse(fees = fee.await(), symbols = pairConfigs.await())
    }

    // Custom service
    @GetMapping("/v3/currencyInfo")
    suspend fun getNetworks(@RequestParam(required = false) currency: String?): List<CurrencyNetworkResponse> {
        return blockchainGatewayProxy.getCurrencyImplementations(currency)
            .groupBy { it.currency }
            .toList()
            .map { pair ->
                CurrencyNetworkResponse(
                    pair.first.symbol,
                    pair.first.name,
                    pair.second.map {
                        CurrencyNetwork(
                            it.chain.name,
                            it.implCurrency.symbol,
                            it.withdrawMin,
                            it.withdrawFee,
                            it.token,
                            it.tokenAddress
                        )
                    }
                )
            }
    }

    // Custom service
    @GetMapping("/v3/currencyInfo/quotes")
    suspend fun getQuoteCurrencies(): List<String> {
        return accountantProxy.getPairConfigs()
            .map { it.rightSideWalletSymbol }
            .distinct()
    }

    // Weight(IP): 1
    @GetMapping("/v3/klines")
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
        val localSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()
        if (validLimit !in 1..1000)
            throw OpexError.InvalidLimitForRecentTrades.exception()

        val i = Interval.findByLabel(interval) ?: throw OpexError.InvalidInterval.exception()

        val list = ArrayList<ArrayList<Any>>()
        marketDataProxy.getCandleInfo(localSymbol, "${i.duration} ${i.unit}", startTime, endTime, validLimit)
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

}
