package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.api.ports.proxy.utils.body
import co.nilin.opex.api.ports.proxy.utils.defaultHeaders
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.api.ports.proxy.utils.withAuth
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import org.springframework.web.util.UriComponentsBuilder

@Component
class WalletProxyImpl(private val restTemplate: RestTemplate) : WalletProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override fun getWallets(uuid: String?, token: String?): List<Wallet> {
        logger.info("fetching wallets for $uuid")
        return restTemplate.getForObject<Array<Wallet>>(
            "$baseUrl/v1/owner/$uuid/wallets",
            defaultHeaders().withAuth(token)
        ).toList()
    }

    override fun getWallet(uuid: String?, token: String?, symbol: String): Wallet {
        logger.info("fetching wallet for $uuid")
        return restTemplate.getForObject<Wallet>(
            "$baseUrl/v1/owner/$uuid/wallets/$symbol",
            defaultHeaders().withAuth(token)
        )
    }

    override fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return restTemplate.getForObject<OwnerLimitsResponse>(
            "$baseUrl/v1/owner/$uuid/limits",
            defaultHeaders().withAuth(token)
        )

    }

    override fun getDepositTransactions(
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
        return restTemplate.postForObject<Array<DepositHistoryResponse>>(
            "$baseUrl/v1/deposit/history",
            body(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime), token)
        ).toList()
    }

    override fun getDepositTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching deposit transaction count for $uuid")
        return restTemplate.getForObject<Long>(
            "$baseUrl/v1/deposit/history/count",
            body(TransactionRequest(currency, startTime, endTime, null, null), token)
        )
    }

    override fun getWithdrawTransactions(
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
        return restTemplate.postForObject<Array<WithdrawHistoryResponse>>(
            "$baseUrl/withdraw/history",
            body(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime), token)
        ).toList()
    }

    override fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching withdraw transaction count for $uuid")
        return restTemplate.postForObject<Long>(
            "$baseUrl/withdraw/history/count",
            TransactionRequest(currency, startTime, endTime, null, null),
            token
        )
    }

    override fun getTransactions(
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
        val request = UserTransactionRequest(
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
        return restTemplate.postForObject<Array<UserTransactionHistory>>(
            "$baseUrl/v2/transaction",
            body(request, token)
        ).toList()
    }

    override fun getTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        category: UserTransactionCategory?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        val request = UserTransactionRequest(null, currency, null, null, category, startTime, endTime, null)
        return restTemplate.postForObject<Long>("$baseUrl/v2/transaction/count", body(request, token))
    }

    override fun getGateWays(
        includeOffChainGateways: Boolean,
        includeOnChainGateways: Boolean,
    ): List<CurrencyGatewayCommand> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/currency/gateways")
            .queryParam("includeOffChainGateways", includeOffChainGateways)
            .queryParam("includeOnChainGateways", includeOnChainGateways)
            .build().toUri()
        return restTemplate.exchange<Array<CurrencyGatewayCommand>>(uri, HttpMethod.GET, noBody()).body?.toList()
            ?: emptyList()
    }

    override fun getCurrencies(): List<CurrencyData> {
        return restTemplate.getForObject<Array<CurrencyData>>("$baseUrl/currency/all", defaultHeaders()).toList()
    }

    override fun getUserTradeTransactionSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/v2/transaction/trade/summary/$uuid")
            .queryParam("startTime", startTime)
            .queryParam("endTime", endTime)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody()).body?.toList()
            ?: emptyList()
    }

    override fun getUserDepositSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/deposit/summary/$uuid")
            .queryParam("startTime", startTime)
            .queryParam("endTime", endTime)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody()).body?.toList()
            ?: emptyList()

    }

    override fun getUserWithdrawSummary(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long?,
        limit: Int?,
    ): List<TransactionSummary> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/withdraw/summary/$uuid")
            .queryParam("startTime", startTime)
            .queryParam("endTime", endTime)
            .queryParam("limit", limit)
            .build().toUri()
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody()).body?.toList()
            ?: emptyList()
    }

    override fun deposit(
        request: RequestDepositBody
    ): TransferResult? {
        val uri =
            UriComponentsBuilder.fromUriString("$baseUrl/deposit/${request.amount}_${request.chain}_${request.symbol}/${request.receiverUuid}_${request.receiverWalletType}")
                .apply {
                    request.description?.let { description -> queryParam("description", description) }
                    request.transferRef?.let { transferRef -> queryParam("transferRef", transferRef) }
                    request.gatewayUuid?.let { gatewayUuid -> queryParam("gatewayUuid", gatewayUuid) }
                }
                .build().toUri()
        return restTemplate.exchange<TransferResult>(uri, HttpMethod.POST, noBody()).body
    }

    override fun requestWithdraw(
        token: String,
        request: RequestWithdrawBody
    ): WithdrawActionResult {
        return restTemplate.postForObject<WithdrawActionResult?>("$baseUrl/withdraw", body(request, token))
            ?: throw OpexError.BadRequest.exception()
    }

    override fun cancelWithdraw(token: String, withdrawId: Long) {
        restTemplate.postForObject<Any>("$baseUrl/withdraw/$withdrawId/cancel", defaultHeaders().withAuth(token))
    }

    override fun findWithdraw(token: String, withdrawId: Long): WithdrawResponse {
        return restTemplate.postForObject<WithdrawResponse?>(
            "$baseUrl/withdraw/$withdrawId",
            defaultHeaders().withAuth(token)
        ) ?: throw OpexError.WithdrawNotFound.exception()
    }

    override fun submitVoucher(
        code: String,
        token: String
    ): SubmitVoucherResponse {
        return restTemplate.postForObject<SubmitVoucherResponse?>(
            "$baseUrl/voucher/$code",
            defaultHeaders().withAuth(token)
        ) ?: throw OpexError.BadRequest.exception()
    }

    override fun getQuoteCurrencies(): List<QuoteCurrency> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/currency/quotes")
            .queryParam("isActive", true)
            .build().toUri()
        return restTemplate.exchange<Array<QuoteCurrency>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse> {
        return restTemplate.postForObject<Array<SwapResponse>>("$baseUrl/v1/swap/history", body(request, token))
            .toList()
    }

    override fun getSwapTransactionsCount(
        token: String,
        request: UserTransactionRequest
    ): Long {
        return restTemplate.postForObject<Long?>("$baseUrl/v1/swap/history/count", body(request, token)) ?: 0
    }
}

