package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*

interface WalletProxy {

    suspend fun getWallets(uuid: String?, token: String?): List<Wallet>

    suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet

    suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

    suspend fun getDepositTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<TransactionHistoryResponse>

    suspend fun getWithdrawTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawHistoryResponse>

    suspend fun getGateWays(
        includeManualGateways: Boolean,
        includeOffChainGateways: Boolean,
        includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand>

}