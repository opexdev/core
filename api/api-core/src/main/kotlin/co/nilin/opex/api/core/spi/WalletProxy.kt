package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.inout.analytics.DailyAmount
import java.math.BigDecimal

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
        status: WithdrawStatus?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawResponse>

    suspend fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        status: WithdrawStatus?,
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


    suspend fun requestWithdraw(
        token: String,
        request: RequestWithdrawBody
    ): WithdrawActionResult

    suspend fun cancelWithdraw(
        token: String,
        withdrawUuid: String
    ): Void?

    suspend fun findWithdraw(
        token: String,
        withdrawUuid: String
    ): WithdrawResponse

    suspend fun submitVoucher(code: String, token: String): SubmitVoucherResponse

    suspend fun getQuoteCurrencies(): List<QuoteCurrency>

    suspend fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse>
    suspend fun getSwapTransactionsCount(token: String, request: UserTransactionRequest): Long

    suspend fun requestWithdrawOTP(token: String, withdrawUuid: String, otpType: OTPType): TempOtpResponse
    suspend fun verifyWithdrawOTP(
        token: String,
        withdrawUuid: String,
        otpType: OTPType,
        otpCode: String
    ): WithdrawActionResult

    suspend fun getWithdrawTransactionsForAdmin(
        token: String,
        request: AdminWithdrawHistoryRequest
    ): List<WithdrawAdminResponse>

    suspend fun getDepositTransactionsForAdmin(
        token: String,
        request: AdminDepositHistoryRequest
    ): List<DepositAdminResponse>

    suspend fun getSwapTransactionsForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<SwapAdminResponse>

    suspend fun getTradeHistoryForAdmin(
        token: String,
        request: AdminTradeHistoryRequest
    ): List<TradeAdminResponse>

    suspend fun getUserTransactionHistoryForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<UserTransactionHistory>

    suspend fun getUsersWallets(
        token: String,
        uuid: String?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletDataResponse>

    suspend fun getSystemWalletsTotal(token: String): List<WalletTotal>
    suspend fun getUsersWalletsTotal(token: String): List<WalletTotal>

    suspend fun acceptWithdraw(token: String, withdrawUuid: String): WithdrawActionResult
    suspend fun doneWithdraw(token: String, withdrawUuid: String, request: WithdrawDoneRequest): WithdrawActionResult
    suspend fun rejectWithdraw(
        token: String,
        withdrawUuid: String,
        request: WithdrawRejectRequest
    ): WithdrawActionResult

    suspend fun withdrawManually(
        token: String,
        symbol: String,
        sourceUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult

    suspend fun depositManually(
        token: String,
        symbol: String,
        receiverUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult

    suspend fun getDailyBalanceLast31Days(token: String, uuid: String): List<DailyAmount>

    suspend fun reserveSwap(token: String, request: TransferReserveRequest): ReservedTransferResponse
    suspend fun finalizeSwap(
        token: String,
        reserveUuid: String,
        description: String?,
        transferRef: String?
    ): TransferResult

    suspend fun getGatewayTerminal(gatewayUuid: String): List<TerminalCommand>
    suspend fun getUsersDetailAssets(limit: Int, offset: Int): List<UserDetailAssetsSnapshot>
    suspend fun getCurrencyLocalizations(token: String, currency: String): CurrencyLocalizationResponse
    suspend fun saveCurrencyLocalizations(
        token: String,
        currency: String,
        currencyLocalizations: List<CurrencyLocalizationCommand>
    ): CurrencyLocalizationResponse

    suspend fun deleteCurrencyLocalization(token: String, id: Long)
    suspend fun getTerminalLocalizations(token: String, terminalUuid: String): TerminalLocalizationResponse
    suspend fun saveTerminalLocalizations(
        token: String,
        terminalUuid: String,
        terminalLocalizations: List<TerminalLocalizationCommand>
    ): TerminalLocalizationResponse

    suspend fun deleteTerminalLocalization(token: String, id: Long)

    suspend fun getOffChainGatewayLocalizations(token: String, gatewayUuid: String): GatewayLocalizationResponse
    suspend fun saveOffChainGatewayLocalizations(
        token: String,
        gatewayUuid: String,
        gatewayLocalizations: List<GatewayLocalizationCommand>
    ): GatewayLocalizationResponse

    suspend fun deleteOffChainGatewayLocalization(token: String, id: Long)
    suspend fun saveTerminal(token: String, terminal: TerminalCommand): TerminalCommand?
    suspend fun updateTerminal(token: String, terminalUuid: String, terminal: TerminalCommand): TerminalCommand?
    suspend fun deleteTerminal(token: String, terminalUuid: String)
    suspend fun getTerminals(token: String): List<TerminalCommand>?
    suspend fun getTerminal(token: String, terminalUuid: String): TerminalCommand?
    suspend fun getAssignedGatewayToTerminal(token: String, terminalUuid: String): List<CurrencyGatewayCommand>?
    suspend fun assignTerminalsToGateway(token: String, gatewayUuid: String, terminal: List<String>)
    suspend fun revokeTerminalsToGateway(token: String, gatewayUuid: String, terminal: List<String>)
    suspend fun addGatewayToCurrency(
        token: String,
        currencySymbol: String,
        gatewayCommand: CurrencyGatewayCommand
    ): CurrencyGatewayCommand?

    suspend fun updateGateway(
        token: String,
        gatewayUuid: String,
        currencySymbol: String,
        gatewayCommand: CurrencyGatewayCommand
    ): CurrencyGatewayCommand?

    suspend fun getGateway(token: String, gatewayUuid: String, currencySymbol: String): CurrencyGatewayCommand?

    suspend fun deleteGateway(token: String, gatewayUuid: String, currencySymbol: String)


}