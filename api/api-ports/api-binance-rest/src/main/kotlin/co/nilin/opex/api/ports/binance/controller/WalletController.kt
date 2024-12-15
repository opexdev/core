package co.nilin.opex.api.ports.binance.controller

import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.inout.TransactionHistoryResponse
import co.nilin.opex.api.core.spi.*
import co.nilin.opex.api.ports.binance.data.*
import co.nilin.opex.api.ports.binance.util.jwtAuthentication
import co.nilin.opex.api.ports.binance.util.tokenValue
import co.nilin.opex.common.OpexError
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@RestController
class WalletController(
    private val walletProxy: WalletProxy,
    private val symbolMapper: SymbolMapper,
    private val marketDataProxy: MarketDataProxy,
    private val accountantProxy: AccountantProxy,
    private val bcGatewayProxy: BlockchainGatewayProxy,
) {

    @GetMapping("/v1/capital/deposit/address")
    suspend fun assignAddress(
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

    @GetMapping("/v1/capital/deposit/hisrec")
    suspend fun getDepositTransactions(
        @RequestParam(required = false)
        coin: String?,
        @RequestParam("network", required = false)
        status: Int?,
        @RequestParam(required = false)
        startTime: Long?,
        @RequestParam(required = false)
        endTime: Long?,
        @RequestParam(required = false)
        offset: Int?,
        @RequestParam(required = false)
        limit: Int?,
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @RequestParam(required = false)
        ascendingByTime: Boolean? = false,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<DepositResponse> {
        val validLimit = limit ?: 1000
        val deposits = walletProxy.getDepositTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            coin,
            startTime ?: null,
            endTime ?: null,
            if (validLimit > 1000 || validLimit < 1) 1000 else validLimit,
            offset ?: 0,
            ascendingByTime
        )
        if (deposits.isEmpty())
            return emptyList()

        val details = bcGatewayProxy.getDepositDetails(deposits.filterNot { it.ref.isNullOrBlank() }.map { it.ref!! })
        return matchDepositsAndDetails(deposits, details)
    }

    @GetMapping("/v1/capital/withdraw/history")
    suspend fun getWithdrawTransactions(
        @RequestParam(required = false)
        coin: String,
        @RequestParam(required = false)
        withdrawOrderId: String?,
        @RequestParam("status", required = false)
        withdrawStatus: Int?,
        @RequestParam(required = false)
        offset: Int?,
        @RequestParam(required = false)
        limit: Int?,
        @RequestParam(required = false)
        startTime: Long?,
        @RequestParam(required = false)
        endTime: Long?,
        @RequestParam(required = false)
        ascendingByTime: Boolean? = false,
        @RequestParam(required = false)
        recvWindow: Long?, //The value cannot be greater than 60000
        @RequestParam
        timestamp: Long,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<WithdrawResponse> {
        val validLimit = limit ?: 1000
        val response = walletProxy.getWithdrawTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            coin,
            startTime ?: null,
            endTime ?: null,
            if (validLimit > 1000 || validLimit < 1) 1000 else validLimit,
            offset ?: 0,
            ascendingByTime
        )
        return response.map {
            val status = when (it.status) {
                "CREATED" -> 0
                "DONE" -> 1
                "REJECTED" -> 2
                else -> -1
            }

            WithdrawResponse(
                it.destAddress ?: "0x0",
                it.amount,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createDate), ZoneId.systemDefault())
                    .toString()
                    .replace("T", " "),
                it.destSymbol ?: "",
                it.withdrawId?.toString() ?: "",
                "",
                it.destNetwork ?: "",
                1,
                status,
                it.appliedFee.toString(),
                3,
                it.destTransactionRef ?: it.withdrawId.toString(),
                if (status == 1 && it.acceptDate != null) it.acceptDate!! else it.createDate
            )
        }
    }


    @PostMapping("/v2/capital/withdraw/history")
    suspend fun getWithdrawTransactionsV2(
        @RequestBody withdrawRequest: WithDrawRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ): List<WithdrawResponse> {
        val validLimit = withdrawRequest.limit ?: 1000
        val response = walletProxy.getWithdrawTransactions(
            securityContext.jwtAuthentication().name,
            securityContext.jwtAuthentication().tokenValue(),
            withdrawRequest.coin,
            withdrawRequest.startTime ?: null,
            withdrawRequest.endTime ?: null,
            if (validLimit > 1000 || validLimit < 1) 1000 else validLimit,
            withdrawRequest.offset ?: 0,
            withdrawRequest.ascendingByTime
        )
        return response.map {
            val status = when (it.status) {
                "CREATED" -> 0
                "DONE" -> 1
                "REJECTED" -> 2
                else -> -1
            }

            WithdrawResponse(
                it.destAddress ?: "0x0",
                it.amount,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.createDate), ZoneId.systemDefault())
                    .toString()
                    .replace("T", " "),
                it.destSymbol ?: "",
                it.withdrawId?.toString() ?: "",
                "",
                it.destNetwork ?: "",
                1,
                status,
                it.appliedFee.toString(),
                3,
                it.destTransactionRef ?: it.withdrawId.toString(),
                if (status == 1 && it.acceptDate != null) it.acceptDate!! else it.createDate
            )
        }
    }

    @GetMapping("/v1/asset/tradeFee")
    suspend fun getPairFees(
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

    private fun matchDepositsAndDetails(
        deposits: List<TransactionHistoryResponse>,
        details: List<DepositDetails>
    ): List<DepositResponse> {
        val detailMap = details.associateBy { it.hash }
        return deposits.associateWith {
            detailMap[it.ref]
        }.mapNotNull { (deposit, detail) ->
            detail?.let {
                DepositResponse(
                    deposit.amount,
                    deposit.currency,
                    detail.chain,
                    1,
                    detail.address,
                    null,
                    deposit.ref ?: deposit.id.toString(),
                    deposit.date,
                    1,
                    "1/1",
                    "1/1",
                    deposit.date
                )
            }
        }
    }
}
