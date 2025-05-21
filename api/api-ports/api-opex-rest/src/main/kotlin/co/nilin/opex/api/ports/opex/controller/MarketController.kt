package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyData
import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairInfoResponse
import co.nilin.opex.api.core.spi.*
import co.nilin.opex.api.ports.opex.data.MarketInfoResponse
import co.nilin.opex.api.ports.opex.data.MarketStatResponse
import co.nilin.opex.common.utils.Interval
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("opexMarketController")
@RequestMapping("/opex/v1/market")
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val marketStatProxy: MarketStatProxy,
    private val marketDataProxy: MarketDataProxy,
    private val walletProxy: WalletProxy,
    private val matchingGatewayProxy: MatchingGatewayProxy,
) {

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
    suspend fun getPairFees(): List<PairFeeResponse> {
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

    private fun getValidLimit(limit: Int?): Int = when {
        limit == null -> 100
        limit > 1000 -> 1000
        limit < 1 -> 1
        else -> limit
    }
}