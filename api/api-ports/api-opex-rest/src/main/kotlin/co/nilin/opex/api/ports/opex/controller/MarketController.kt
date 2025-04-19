package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairInfoResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.spi.MatchingGatewayProxy
import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("opexMarketController")
@RequestMapping("/opex/v1/market")
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val walletProxy: WalletProxy,
    private val matchingGatewayProxy: MatchingGatewayProxy,
) {

    @GetMapping("/currency")
    suspend fun getCurrencies(){
        walletProxy
    }

    @GetMapping("/pair")
    suspend fun getPairs(): List<PairInfoResponse> {
        val pairSettings = matchingGatewayProxy.getPairSettings().associateBy { it.pair }

        return accountantProxy.getPairConfigs().mapNotNull { config ->
            pairSettings[config.pair]?.run {
                PairInfoResponse(
                    pair = config.pair,
                    leftSideWalletSymbol = config.leftSideWalletSymbol,
                    rightSideWalletSymbol = config.rightSideWalletSymbol,
                    leftSideFraction = config.leftSideFraction,
                    rightSideFraction = config.rightSideFraction,
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
        @RequestParam(defaultValue = "true") includeManualGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOffChainGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand> {
        return walletProxy.getGateWays(includeManualGateways, includeOffChainGateways, includeOnChainGateways)
    }

    @GetMapping("/pair/fee")
    suspend fun getPairFees(): List<PairFeeResponse> {
        return accountantProxy.getFeeConfigs()
    }
}