package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.inout.analytics.DailyAmount
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.api.ports.proxy.data.WithdrawTransactionRequest
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Component
class WalletProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : WalletProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override suspend fun getWallets(uuid: String?, token: String?): List<Wallet> {
        logger.info("fetching wallets for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/v1/owner/$uuid/wallets")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<Wallet>()
                .collectList()
                .awaitSingle()
        }
    }

    override suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet {
        logger.info("fetching wallet for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/v1/owner/$uuid/wallets/$symbol")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Wallet>()
                .awaitSingle()
        }
    }

    override suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/v1/owner/$uuid/limits")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<OwnerLimitsResponse>()
                .awaitSingle()
        }
    }

    override suspend fun getDepositTransactions(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<DepositHistoryResponse> {
        logger.info("fetching deposit transaction history for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/v1/deposit/history")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(Mono.just(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<DepositHistoryResponse>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getDepositTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching deposit transaction count for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/v1/deposit/history/count")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(Mono.just(TransactionRequest(currency, startTime, endTime, null, null)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Long>()
                .awaitFirstOrElse { 0L }
        }
    }

    override suspend fun getWithdrawTransactions(
        uuid: String,
        token: String,
        currency: String?,
        staus: WithdrawStatus?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawResponse> {
        logger.info("fetching withdraw transaction history for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/withdraw/history")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(
                    Mono.just(
                        WithdrawTransactionRequest(
                            currency,
                            startTime,
                            endTime,
                            limit,
                            offset,
                            ascendingByTime,
                            staus
                        )
                    )
                )
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<WithdrawResponse>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        status: WithdrawStatus?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching withdraw transaction count for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/withdraw/history/count")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(Mono.just(WithdrawTransactionRequest(currency, startTime, endTime, null, null, null, status)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<Long>()
                .awaitFirstOrElse { 0L }
        }
    }

    override suspend fun getTransactions(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<UserTransactionHistory> {
        return webClient.post()
            .uri("$baseUrl/v2/transaction")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(
                Mono.just(
                    UserTransactionRequest(
                        null,
                        currency,
                        null,
                        null,
                        category,
                        startTime,
                        endTime,
                        limit,
                        offset,
                        ascendingByTime == true,
                        null
                    )
                )
            )
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<UserTransactionHistory>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        return webClient.post()
            .uri("$baseUrl/v2/transaction/count")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(
                Mono.just(
                    UserTransactionRequest(
                        null,
                        currency,
                        null,
                        null,
                        category,
                        startTime,
                        endTime,
                        null
                    )
                )
            )
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Long>()
            .awaitFirstOrElse { 0L }
    }

    override suspend fun getGateWays(
        includeOffChainGateways: Boolean,
        includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/currency/gateways") {
                    it.queryParam("includeOffChainGateways", includeOffChainGateways)
                    it.queryParam("includeOnChainGateways", includeOnChainGateways)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<CurrencyGatewayCommand>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getCurrencies(): List<CurrencyData> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/currency/all")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<CurrencyData>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getUserTradeTransactionSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/v2/transaction/trade/summary/$uuid") {
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("limit", limit)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<TransactionSummary>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getUserDepositSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/deposit/summary/$uuid") {
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("limit", limit)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<TransactionSummary>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getUserWithdrawSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/withdraw/summary/$uuid") {
                    it.queryParam("startTime", startTime)
                    it.queryParam("endTime", endTime)
                    it.queryParam("limit", limit)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<TransactionSummary>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun requestWithdraw(
        token: String,
        request: RequestWithdrawBody
    ): WithdrawActionResult {
        return webClient.post()
            .uri("$baseUrl/withdraw")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawActionResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun cancelWithdraw(token: String, withdrawUuid: String): Void? {
        return webClient.post()
            .uri("$baseUrl/withdraw/$withdrawUuid/cancel")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(Void::class.java)
            .awaitFirstOrNull()
    }

    override suspend fun findWithdraw(token: String, withdrawUuid: String): WithdrawResponse {
        return webClient.get()
            .uri("$baseUrl/withdraw/$withdrawUuid")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawResponse>()
            .awaitFirstOrElse { throw OpexError.WithdrawNotFound.exception() }
    }

    override suspend fun submitVoucher(
        code: String,
        token: String
    ): SubmitVoucherResponse {
        return webClient.put()
            .uri("$baseUrl/voucher/$code")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<SubmitVoucherResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getQuoteCurrencies(): List<QuoteCurrency> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/currency/quotes") {
                    it.queryParam("isActive", true)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<QuoteCurrency>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse> {
        return webClient.post()
            .uri("$baseUrl/v1/swap/history")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<SwapResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getSwapTransactionsCount(
        token: String,
        request: UserTransactionRequest
    ): Long {
        return webClient.post()
            .uri("$baseUrl/v1/swap/history/count")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Long>()
            .awaitFirstOrElse { 0L }
    }

    override suspend fun requestWithdrawOTP(
        token: String,
        withdrawUuid: String,
        otpType: OTPType
    ): TempOtpResponse {
        return webClient.post()
            .uri("$baseUrl/withdraw/${withdrawUuid}/otp/${otpType}/request")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TempOtpResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun verifyWithdrawOTP(
        token: String,
        withdrawUuid: String,
        otpType: OTPType,
        otpCode: String
    ): WithdrawActionResult {
        return webClient.post()
            .uri("$baseUrl/withdraw/${withdrawUuid}/otp/${otpType}/verify?otpCode=${otpCode}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawActionResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getWithdrawTransactionsForAdmin(
        token: String,
        request: AdminWithdrawHistoryRequest
    ): List<WithdrawAdminResponse> {
        return webClient.post()
            .uri("$baseUrl/admin/withdraw/history?offset=${request.offset}&size=${request.limit}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<WithdrawAdminResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getDepositTransactionsForAdmin(
        token: String,
        request: AdminDepositHistoryRequest
    ): List<DepositAdminResponse> {
        return webClient.post()
            .uri("$baseUrl/admin/deposit/history?offset=${request.offset}&size=${request.limit}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<DepositAdminResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getSwapTransactionsForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<SwapAdminResponse> {
        return webClient.post()
            .uri("$baseUrl/admin/v1/swap/history")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<SwapAdminResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getTradeHistoryForAdmin(
        token: String,
        request: AdminTradeHistoryRequest
    ): List<TradeAdminResponse> {
        return webClient.post()
            .uri("$baseUrl/admin/v2/transaction/trades")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<TradeAdminResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getUserTransactionHistoryForAdmin(
        token: String,
        request: UserTransactionRequest
    ): List<UserTransactionHistory> {
        return webClient.post()
            .uri("$baseUrl/admin/v2/transaction/history")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<UserTransactionHistory>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getUsersWallets(
        token: String,
        uuid: String?,
        currency: String?,
        excludeSystem: Boolean,
        limit: Int,
        offset: Int
    ): List<WalletDataResponse> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/stats/v2/wallets") { builder ->
                    uuid?.let { builder.queryParam("uuid", it) }
                    currency?.let { builder.queryParam("currency", it) }
                    builder.queryParam("excludeSystem", excludeSystem)
                    builder.queryParam("limit", limit)
                    builder.queryParam("offset", offset)
                    builder.build()
                }
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<WalletDataResponse>()
                .collectList()
                .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get users wallets") }
        }
    }

    override suspend fun getSystemWalletsTotal(token: String): List<WalletTotal> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/stats/wallets/system/total")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<WalletTotal>()
                .collectList()
                .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get system wallets total") }
        }
    }

    override suspend fun getUsersWalletsTotal(token: String): List<WalletTotal> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/stats/wallets/user/total")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<WalletTotal>()
                .collectList()
                .awaitFirstOrElse { throw OpexError.BadRequest.exception("Failed to get users wallets total") }
        }
    }

    override suspend fun acceptWithdraw(
        token: String,
        withdrawUuid: String
    ): WithdrawActionResult {
        return webClient.post()
            .uri("$baseUrl/admin/withdraw/${withdrawUuid}/accept")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawActionResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun doneWithdraw(
        token: String,
        withdrawUuid: String,
        request: WithdrawDoneRequest
    ): WithdrawActionResult {
        return webClient.post()
            .uri("$baseUrl/admin/withdraw/${withdrawUuid}/done")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawActionResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun rejectWithdraw(
        token: String,
        withdrawUuid: String,
        request: WithdrawRejectRequest
    ): WithdrawActionResult {
        return webClient.post()
            .uri("$baseUrl/admin/withdraw/${withdrawUuid}/reject")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<WithdrawActionResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun withdrawManually(
        token: String,
        symbol: String,
        sourceUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult {
        return webClient.post()
            .uri("$baseUrl/admin/withdraw/manually/${amount}_${symbol}/${sourceUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun depositManually(
        token: String,
        symbol: String,
        receiverUuid: String,
        amount: BigDecimal,
        request: ManualTransferRequest
    ): TransferResult {
        return webClient.post()
            .uri("$baseUrl/admin/deposit/manually/${amount}_${symbol}/${receiverUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getDailyBalanceLast31Days(
        token: String,
        uuid: String
    ): List<DailyAmount> {

        logger.info("fetching daily balance stats for {}", uuid)

        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/stats/balance/$uuid")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .retrieve()
                .onStatus({ it.isError }) { it.createException() }
                .bodyToMono<List<DailyAmount>>()
                .awaitSingle()
        }
    }

    override suspend fun reserveSwap(
        token: String,
        request: TransferReserveRequest
    ): ReservedTransferResponse {
        return webClient.post()
            .uri("$baseUrl/v3/transfer/reserve")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<ReservedTransferResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun finalizeSwap(
        token: String,
        reserveUuid: String,
        description: String?,
        transferRef: String?
    ): TransferResult {
        return webClient.post()
            .uri("$baseUrl/v3/transfer/${reserveUuid}") {
                it.queryParam("description", description)
                it.queryParam("transferRef", transferRef)
                it.build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TransferResult>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getGatewayTerminal(gatewayUuid: String): List<TerminalCommand> {
        return webClient.get()
            .uri("$baseUrl/currency/gateway/${gatewayUuid}/terminal")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<TerminalCommand>>()
            .awaitFirstOrElse { throw OpexError.TerminalNotFound.exception() }

    }

    override suspend fun getUsersDetailAssets(
        limit: Int,
        offset: Int
    ): List<UserDetailAssetsSnapshot> {
        return withContext(ProxyDispatchers.wallet) {
            webClient.get()
                .uri("$baseUrl/stats/detail-assets") {
                    it.queryParam("limit", limit)
                    it.queryParam("offset", offset)
                    it.build()
                }.accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<UserDetailAssetsSnapshot>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getCurrencyLocalizations(
        token: String,
        currency: String
    ): CurrencyLocalizationResponse {
        return webClient.get()
            .uri("$baseUrl/currency/${currency}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CurrencyLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun saveCurrencyLocalizations(
        token: String,
        currency: String,
        currencyLocalizations: List<CurrencyLocalizationCommand>
    ): CurrencyLocalizationResponse {
        return webClient.post()
            .uri("$baseUrl/currency/${currency}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(currencyLocalizations))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CurrencyLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteCurrencyLocalization(token: String, id: Long) {
        webClient.delete()
            .uri("$baseUrl/currency/localization/${id}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getTerminalLocalizations(
        token: String,
        terminalUuid: String
    ): TerminalLocalizationResponse {
        return webClient.get()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TerminalLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun saveTerminalLocalizations(
        token: String,
        terminalUuid: String,
        terminalLocalizations: List<TerminalLocalizationCommand>
    ): TerminalLocalizationResponse {
        return webClient.post()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(terminalLocalizations))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TerminalLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteTerminalLocalization(token: String, id: Long) {
        webClient.delete()
            .uri("$baseUrl/admin/deposit/terminal/localization/${id}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getOffChainGatewayLocalizations(
        token: String,
        gatewayUuid: String
    ): GatewayLocalizationResponse {
        return webClient.get()
            .uri("$baseUrl/offchain-gateway/${gatewayUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GatewayLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun saveOffChainGatewayLocalizations(
        token: String,
        gatewayUuid: String,
        gatewayLocalizations: List<GatewayLocalizationCommand>
    ): GatewayLocalizationResponse {
        return webClient.post()
            .uri("$baseUrl/offchain-gateway/${gatewayUuid}/localization")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(gatewayLocalizations))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<GatewayLocalizationResponse>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteOffChainGatewayLocalization(token: String, id: Long) {
        webClient.delete()
            .uri("$baseUrl/offchain-gateway/localization/${id}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun saveTerminal(
        token: String,
        terminal: TerminalCommand
    ): TerminalCommand? {
        return webClient.post()
            .uri("$baseUrl/admin/deposit/terminal")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(terminal))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TerminalCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun updateTerminal(
        token: String,
        terminalUuid: String,
        terminal: TerminalCommand
    ): TerminalCommand? {
        return webClient.put()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(terminal))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TerminalCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteTerminal(token: String, terminalUuid: String) {
        webClient.delete()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun getTerminals(token: String): List<TerminalCommand>? {
        return webClient.get()
            .uri("$baseUrl/admin/deposit/terminal")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<TerminalCommand>()
            .collectList()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getTerminal(
        token: String,
        terminalUuid: String
    ): TerminalCommand? {
        return webClient.get()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TerminalCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getAssignedGatewayToTerminal(
        token: String,
        terminalUuid: String
    ): List<CurrencyGatewayCommand>? {
        return webClient.get()
            .uri("$baseUrl/admin/deposit/terminal/${terminalUuid}/gateway")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<CurrencyGatewayCommand>()
            .collectList()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun assignTerminalsToGateway(
        token: String,
        gatewayUuid: String,
        terminal: List<String>
    ) {
        webClient.post()
            .uri("$baseUrl/currency/gateway/${gatewayUuid}/terminal")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(terminal))
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun revokeTerminalsToGateway(
        token: String,
        gatewayUuid: String,
        terminal: List<String>
    ) {
        webClient.method(HttpMethod.DELETE)
            .uri("$baseUrl/currency/gateway/${gatewayUuid}/terminal")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .bodyValue(terminal)
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }

    override suspend fun addGatewayToCurrency(
        token: String,
        currencySymbol: String,
        gatewayCommand: CurrencyGatewayCommand
    ): CurrencyGatewayCommand? {
        return webClient.post()
            .uri("$baseUrl/currency/${currencySymbol}/gateway")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(gatewayCommand))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CurrencyGatewayCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun updateGateway(
        token: String,
        gatewayUuid: String,
        currencySymbol: String,
        gatewayCommand: CurrencyGatewayCommand
    ): CurrencyGatewayCommand? {
        return webClient.put()
            .uri("$baseUrl/currency/${currencySymbol}/gateway/${gatewayUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(gatewayCommand))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CurrencyGatewayCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun getGateway(
        token: String,
        gatewayUuid: String,
        currencySymbol: String
    ): CurrencyGatewayCommand? {
        return webClient.get()
            .uri("$baseUrl/currency/${currencySymbol}/gateway/${gatewayUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<CurrencyGatewayCommand>()
            .awaitFirstOrElse { throw OpexError.BadRequest.exception() }
    }

    override suspend fun deleteGateway(
        token: String,
        gatewayUuid: String,
        currencySymbol: String
    ) {
        webClient.delete()
            .uri("$baseUrl/currency/${currencySymbol}/gateway/${gatewayUuid}")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.createException()
            }
            .awaitBodilessEntity()
    }
}

