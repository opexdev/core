package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class WalletProxyImpl(private val webClient: WebClient) : WalletProxy {

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
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?,
    ): List<WithdrawHistoryResponse> {
        logger.info("fetching withdraw transaction history for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/withdraw/history")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(Mono.just(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime)))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToFlux<WithdrawHistoryResponse>()
                .collectList()
                .awaitFirstOrElse { emptyList() }
        }
    }

    override suspend fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching withdraw transaction count for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/withdraw/history/count")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(Mono.just(TransactionRequest(currency, startTime, endTime, null, null)))
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
            .body(Mono.just(UserTransactionRequest(currency, category, startTime, endTime)))
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

    override suspend fun deposit(
        request: RequestDepositBody
    ): TransferResult? {
        return withContext(ProxyDispatchers.wallet) {
            webClient.post()
                .uri("$baseUrl/deposit/${request.amount}_${request.chain}_${request.symbol}/${request.receiverUuid}_${request.receiverWalletType}") {
                    it.apply {
                        request.description?.let { description -> queryParam("description", description) }
                        request.transferRef?.let { transferRef -> queryParam("transferRef", transferRef) }
                        request.gatewayUuid?.let { gatewayUuid -> queryParam("gatewayUuid", gatewayUuid) }
                    }.build()
                }.accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<TransferResult>()
                .awaitFirstOrNull()
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

    override suspend fun cancelWithdraw(token: String, withdrawId: Long): Void? {
        return webClient.post()
            .uri("$baseUrl/withdraw/$withdrawId/cancel")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(Void::class.java)
            .awaitFirstOrNull()
    }

    override suspend fun findWithdraw(token: String, withdrawId: Long): WithdrawResponse {
        return webClient.get()
            .uri("$baseUrl/withdraw/$withdrawId")
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
}

