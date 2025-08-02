package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*

interface WalletProxy {

    fun getWallets(uuid: String?, token: String?): List<Wallet>

    fun getWallet(uuid: String?, token: String?, symbol: String): Wallet

    fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

    fun getDepositTransactions(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<DepositHistoryResponse>

    fun getDepositTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    fun getWithdrawTransactions(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawHistoryResponse>

    fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    fun getTransactions(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<UserTransactionHistory>

    fun getTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    fun getGateWays(
        includeOffChainGateways: Boolean,
        includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand>

    fun getCurrencies(): List<CurrencyData>

    fun getUserTradeTransactionSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    fun getUserDepositSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    fun getUserWithdrawSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    fun deposit(
        request: RequestDepositBody
    ): TransferResult?

    fun requestWithdraw(
        token: String,
        request: RequestWithdrawBody
    ): WithdrawActionResult

    fun cancelWithdraw(
        token: String,
        withdrawId: Long
    )

    fun findWithdraw(
        token: String,
        withdrawId: Long
    ): WithdrawResponse

    fun submitVoucher(code: String, token: String): SubmitVoucherResponse

    fun getQuoteCurrencies(): List<QuoteCurrency>

    fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse>
    fun getSwapTransactionsCount(token: String, request: UserTransactionRequest): Long
}