package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.inout.TransactionHistoryResponse
import co.nilin.opex.api.core.inout.Wallet
import co.nilin.opex.api.core.inout.WithdrawHistoryResponse

interface WalletProxy {

    suspend fun getWallets(uuid: String?, token: String?): List<Wallet>

    suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet

    suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

    suspend fun getDepositTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<TransactionHistoryResponse>

    suspend fun getWithdrawTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long,
        endTime: Long,
        limit: Int,
        offset: Int
    ): List<WithdrawHistoryResponse>

}