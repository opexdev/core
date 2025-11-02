package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.spi.MarketDataProxy
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.binance.data.AssetResponse
import co.nilin.opex.api.ports.binance.data.AssetsEstimatedValue
import co.nilin.opex.common.security.jwtAuthentication
import co.nilin.opex.common.security.tokenValue
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController("walletBinanceController")
class WalletController(
    private val walletProxy: WalletProxy,
    private val marketDataProxy: MarketDataProxy,
) {
    @GetMapping("/v1/asset/getUserAsset")
    suspend fun getUserAssets(
        @CurrentSecurityContext
        securityContext: SecurityContext,
        @RequestParam(required = false)
        symbol: String?,
        @RequestParam(required = false)
        quoteAsset: String?,
        @RequestParam(required = false)
        calculateEvaluation: Boolean?
    ): List<AssetResponse> {
        val auth = securityContext.jwtAuthentication()
        val result = arrayListOf<AssetResponse>()

        if (symbol != null) {
            val wallet = walletProxy.getWallet(auth.name, auth.tokenValue(), symbol.uppercase())
            result.add(AssetResponse(wallet.asset, wallet.balance, wallet.locked, wallet.withdraw))
        } else {
            result.addAll(
                walletProxy.getWallets(auth.name, auth.tokenValue())
                    .map { AssetResponse(it.asset, it.balance, it.locked, it.withdraw) }
            )
        }

        if (quoteAsset == null)
            return result

        val prices = marketDataProxy.getBestPriceForSymbols(
            result.map { "${it.asset.uppercase()}_${quoteAsset.uppercase()}" }
        ).associateBy { it.symbol.split("_")[0] }

        result.associateWith { prices[it.asset] }
            .forEach { (asset, price) -> asset.valuation = price?.bidPrice ?: BigDecimal.ZERO }

        if (calculateEvaluation == true)
            result.forEach {
                it.free = it.free.multiply(it.valuation)
                it.locked = it.locked.multiply(it.valuation)
                it.withdrawing = it.withdrawing.multiply(it.valuation)
            }

        return result
    }

    @GetMapping("/v1/asset/estimatedValue")
    suspend fun assetsEstimatedValue(
        @CurrentSecurityContext
        securityContext: SecurityContext,
        @RequestParam
        quoteAsset: String
    ): AssetsEstimatedValue {
        val auth = securityContext.jwtAuthentication()
        val wallets = walletProxy.getWallets(auth.name, auth.tokenValue())
        val rates = marketDataProxy.getBestPriceForSymbols(
            wallets.map { "${it.asset.uppercase()}_${quoteAsset.uppercase()}" }
        ).associateBy { it.symbol.split("_")[0] }

        var value = BigDecimal.ZERO
        val zeroAssets = arrayListOf<String>()
        wallets.filter { !it.asset.equals(quoteAsset, true) }
            .associateWith { rates[it.asset] }
            .forEach { (asset, price) ->
                if (price == null || (price.bidPrice ?: BigDecimal.ZERO) == BigDecimal.ZERO)
                    zeroAssets.add(asset.asset)
                else
                    value += asset.balance.multiply(price.bidPrice)
            }

        // Add quote asset balance with rate of 1
        wallets.find { it.asset.equals(quoteAsset, true) }?.let { value += it.balance }
        return AssetsEstimatedValue(value, quoteAsset.uppercase(), zeroAssets)
    }
}
