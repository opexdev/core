package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*

interface WalletProxy {

    suspend fun getWallets(uuid: String?, token: String?): List<Wallet>

    suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet

    suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse

    suspend fun getDepositTransactions(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<DepositHistoryResponse>

    suspend fun getDepositTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    suspend fun getWithdrawTransactions(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawHistoryResponse>

    suspend fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    suspend fun getTransactions(
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

    suspend fun getTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
    ): Long

    suspend fun getGateWays(
        includeOffChainGateways: Boolean,
        includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand>

    suspend fun getCurrencies(): List<CurrencyData>

    suspend fun getUserTradeTransactionSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    suspend fun getUserDepositSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    suspend fun getUserWithdrawSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary>

    suspend fun deposit(
        request: RequestDepositBody
    ): TransferResult?

    suspend fun requestWithdraw(
        token: String,
        request: RequestWithdrawBody
    ): WithdrawActionResult

    suspend fun cancelWithdraw(
        token: String,
        withdrawId: Long
    ): Void?

    suspend fun findWithdraw(
        token: String,
        withdrawId: Long
    ): WithdrawResponse

    suspend fun submitVoucher(code: String, token: String): SubmitVoucherResponse

    suspend fun getQuoteCurrencies(): List<QuoteCurrency>

    suspend fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse>
    suspend fun getSwapTransactionsCount(token: String, request: UserTransactionRequest): Long

    suspend fun requestWithdrawOTP(token: String, withdrawId: Long, otpType: OTPType): TempOtpResponse
    suspend fun verifyWithdrawOTP(
        token: String,
        withdrawId: Long,
        otpType: OTPType,
        otpCode: String
    ): WithdrawActionResult

    suspend fun getWithdrawTransactionsForAdmin(
        token: String,
        request: AdminSearchWithdrawRequest
    ): List<WithdrawHistoryResponse>

    suspend fun getDepositTransactionsForAdmin(
        token: String,
        request: AdminSearchDepositRequest
    ): List<DepositHistoryResponse>

    suspend fun getSwapTransactionsForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<SwapHistoryResponse>

    suspend fun getTransactionHistoryForAdmin(
        token: String,
        request: AdminTransactionHistoryRequest
    ): List<AdminTransactionHistory>

    suspend fun getUserTransactionHistoryForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<UserTransactionHistory>
}