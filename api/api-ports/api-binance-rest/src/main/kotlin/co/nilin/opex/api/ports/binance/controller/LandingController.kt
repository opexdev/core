package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.spi.GlobalMarketProxy
import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.core.spi.MarketStatProxy
import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.core.utils.Interval
import co.nilin.opex.api.ports.binance.data.GlobalPriceResponse
import co.nilin.opex.api.ports.binance.data.MarketInfoResponse
import co.nilin.opex.api.ports.binance.data.MarketStatResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController // Custom service
@RequestMapping("/v1/landing")
class LandingController(
    private val marketStatProxy: MarketStatProxy,
    private val marketDataProxy: MarketDataProxy,
    private val globalMarketProxy: GlobalMarketProxy,
    private val symbolMapper: SymbolMapper
) {

    private val logger = LoggerFactory.getLogger(LandingController::class.java)

    @GetMapping("/globalPrices")
    suspend fun getCurrencyPrices(@RequestParam usdSymbol: String): GlobalPriceResponse {
        val irtUSDPrice = marketDataProxy.getExternalCurrencyRates("IRT", usdSymbol)
        val globalPrice = try {
            globalMarketProxy.getPrices(symbolMapper.symbolToAliasMap().entries.map { it.value })
        } catch (e: Exception) {
            logger.error("Could not fetch prices")
            emptyList()
        }

        return GlobalPriceResponse(irtUSDPrice, globalPrice)
    }

    @GetMapping("/marketStats")
    suspend fun getMarketStats(
        @RequestParam interval: String,
        @RequestParam(required = false) limit: Int?
    ): MarketStatResponse {
        val since = (Interval.findByLabel(interval) ?: Interval.Week).getDate().time
        val validLimit = getValidLimit(limit)
        val symbols = symbolMapper.symbolToAliasMap()


        return MarketStatResponse(
            marketStatProxy.getMostIncreasedInPricePairs(since, validLimit).onEach {
                symbols[it.symbol]?.let { s -> it.symbol = s }
            },
            marketStatProxy.getMostDecreasedInPricePairs(since, validLimit).onEach {
                symbols[it.symbol]?.let { s -> it.symbol = s }
            },
            marketStatProxy.getHighestVolumePair(since)?.apply { symbols[symbol]?.let { symbol = it } },
            marketStatProxy.getTradeCountPair(since)?.apply { symbols[symbol]?.let { symbol = it } }
        )
    }

    @GetMapping("/exchangeInfo")
    suspend fun marketInfo(@RequestParam interval: String): MarketInfoResponse {
        val since = (Interval.findByLabel(interval) ?: Interval.ThreeMonth).getDate().time
        return MarketInfoResponse(
            marketDataProxy.countActiveUsers(since),
            marketDataProxy.countTotalOrders(since),
            marketDataProxy.countTotalTrades(since),
        )
    }

    private fun getValidLimit(limit: Int?): Int = when {
        limit == null -> 100
        limit > 1000 -> 1000
        limit < 1 -> 1
        else -> limit
    }

}