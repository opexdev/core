package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.spi.*
import co.nilin.opex.api.ports.binance.data.AssetResponse
import co.nilin.opex.api.ports.binance.data.AssetsEstimatedValue
import co.nilin.opex.api.ports.binance.data.AssignAddressResponse
import co.nilin.opex.api.ports.binance.data.PairFeeResponse
import co.nilin.opex.api.ports.binance.util.jwtAuthentication
import co.nilin.opex.api.ports.binance.util.tokenValue
import co.nilin.opex.common.OpexError
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController("walletBinanceController")
class WalletController(
    private val walletProxy: WalletProxy,
    private val symbolMapper: SymbolMapper,
    private val marketDataProxy: MarketDataProxy,
    private val accountantProxy: AccountantProxy,
    private val bcGatewayProxy: BlockchainGatewayProxy,
) {

    @GetMapping("/v1/capital/deposit/address")
    fun assignAddress(
        @RequestParam
        coin: String,
        @RequestParam
        network: String,
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): AssignAddressResponse {
        val response = bcGatewayProxy.assignAddress(securityContext.jwtAuthentication().name, coin, network)
        val address = response?.addresses
        if (address.isNullOrEmpty()) throw OpexError.InternalServerError.exception()
        return AssignAddressResponse(address[0].address, coin, network, "", "")
    }

    @GetMapping("/v1/asset/tradeFee")
    fun getPairFees(
        @RequestParam(required = false)
        symbol: String?,
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long
    ): List<PairFeeResponse> {
        return if (symbol != null) {
            val internalSymbol = symbolMapper.toInternalSymbol(symbol) ?: throw OpexError.SymbolNotFound.exception()

            val fee = accountantProxy.getFeeConfig(internalSymbol)
            arrayListOf<PairFeeResponse>().apply {
                add(
                    PairFeeResponse(
                        symbol,
                        fee.makerFee.toDouble(),
                        fee.takerFee.toDouble()
                    )
                )
            }
        } else
            accountantProxy.getFeeConfigs()
                .distinctBy { it.pair }
                .map {
                    PairFeeResponse(
                        symbolMapper.fromInternalSymbol(it.pair) ?: it.pair,
                        it.makerFee.toDouble(),
                        it.takerFee.toDouble()
                    )
                }
    }

    @GetMapping("/v1/asset/getUserAsset")
    fun getUserAssets(
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
