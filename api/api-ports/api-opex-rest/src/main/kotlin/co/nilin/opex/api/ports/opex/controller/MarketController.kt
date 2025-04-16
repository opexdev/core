package co.nilin.opex.api.ports.opex.controller

import co.nilin.opex.api.core.inout.CurrencyGatewayCommand
import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.spi.WalletProxy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("opexMarketController")
@RequestMapping("/opex/v1/market")
class MarketController(
    private val accountantProxy: AccountantProxy,
    private val walletProxy: WalletProxy,
) {

    //TODO
    @GetMapping("/pair")
    suspend fun getPair() {
        print("GETTING MARKET PAIR")
    }

    @GetMapping("/currency/gateway")
    suspend fun getCurrencyGateway(
        @RequestParam(defaultValue = "true") includeManualGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOffChainGateways: Boolean,
        @RequestParam(defaultValue = "true") includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand> {
        return walletProxy.getGateWays(includeManualGateways, includeOffChainGateways, includeOnChainGateways)
    }

    @GetMapping("/pair/fee")
    suspend fun getPairFee(): List<PairFeeResponse> {
        return accountantProxy.getFeeConfigs()
    }
}