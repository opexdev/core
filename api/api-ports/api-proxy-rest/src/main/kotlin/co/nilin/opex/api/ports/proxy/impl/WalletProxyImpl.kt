package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.*
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.api.ports.proxy.utils.body
import co.nilin.opex.api.ports.proxy.utils.noBody
import co.nilin.opex.common.OpexError
import co.nilin.opex.common.utils.LoggerDelegate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.math.BigDecimal

@Component
class WalletProxyImpl(private val restTemplate: RestTemplate) : WalletProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override fun getWallets(uuid: String?, token: String?): List<Wallet> {
        logger.info("fetching wallets for $uuid")
        return restTemplate.exchange<Array<Wallet>>(
            "$baseUrl/v1/owner/$uuid/wallets",
            HttpMethod.GET,
            noBody(token)
        ).body?.toList() ?: emptyList()
    }

    override fun getWallet(uuid: String?, token: String?, symbol: String): Wallet {
        logger.info("fetching wallet for $uuid")
        return restTemplate.exchange<Wallet?>(
            "$baseUrl/v1/owner/$uuid/wallets/$symbol",
            HttpMethod.GET,
            noBody(token)
        ).body ?: Wallet(symbol, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
    }

    override fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return restTemplate.exchange<OwnerLimitsResponse>(
            "$baseUrl/v1/owner/$uuid/limits",
            HttpMethod.GET,
            noBody(token)
        ).body!!
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
        return restTemplate.exchange<Array<DepositHistoryResponse>>(
            "$baseUrl/v1/deposit/history",
            HttpMethod.POST,
            body(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime), token)
        ).body?.toList() ?: emptyList()
    }

    override fun getDepositTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching deposit transaction count for $uuid")
        return restTemplate.exchange<Long>(
            "$baseUrl/v1/deposit/history/count",
            HttpMethod.POST,
            body(TransactionRequest(currency, startTime, endTime, null, null), token)
        ).body ?: 0L
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
        return restTemplate.exchange<Array<WithdrawHistoryResponse>>(
            "$baseUrl/withdraw/history",
            HttpMethod.POST,
            body(TransactionRequest(currency, startTime, endTime, limit, offset, ascendingByTime), token)
        ).body?.toList() ?: emptyList()
    }

    override fun getWithdrawTransactionsCount(
        uuid: String,
        token: String,
        currency: String?,
        startTime: Long?,
        endTime: Long?,
    ): Long {
        logger.info("fetching withdraw transaction count for $uuid")
        return restTemplate.exchange<Long>(
            "$baseUrl/withdraw/history/count",
            HttpMethod.POST,
            body(TransactionRequest(currency, startTime, endTime, null, null), token)
        ).body ?: 0L
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
        return restTemplate.exchange<Array<UserTransactionHistory>>(
            "$baseUrl/v2/transaction",
            HttpMethod.POST,
            body(request, token)
        ).body?.toList() ?: emptyList()
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
        return restTemplate.exchange<Long>("$baseUrl/v2/transaction/count", HttpMethod.POST, body(request, token)).body
            ?: 0
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
        return restTemplate.exchange<Array<CurrencyData>>(
            "$baseUrl/currency/all",
            HttpMethod.GET,
            noBody()
        ).body?.toList() ?: emptyList()
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
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody(token)).body?.toList()
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
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody(token)).body?.toList()
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
        return restTemplate.exchange<Array<TransactionSummary>>(uri, HttpMethod.GET, noBody(token)).body?.toList()
            ?: emptyList()
    }

    override fun deposit(request: RequestDepositBody): TransferResult? {
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
        return restTemplate.exchange<WithdrawActionResult?>(
            "$baseUrl/withdraw",
            HttpMethod.POST,
            body(request, token)
        ).body ?: throw OpexError.BadRequest.exception()
    }

    override fun cancelWithdraw(token: String, withdrawId: Long) {
        restTemplate.exchange<Any>("$baseUrl/withdraw/$withdrawId/cancel", HttpMethod.POST, noBody(token))
    }

    override fun findWithdraw(token: String, withdrawId: Long): WithdrawResponse {
        return restTemplate.exchange<WithdrawResponse?>(
            "$baseUrl/withdraw/$withdrawId",
            HttpMethod.POST,
            noBody(token)
        ).body ?: throw OpexError.WithdrawNotFound.exception()
    }

    override fun submitVoucher(
        code: String,
        token: String
    ): SubmitVoucherResponse {
        return restTemplate.exchange<SubmitVoucherResponse?>(
            "$baseUrl/voucher/$code",
            HttpMethod.POST,
            noBody(token)
        ).body ?: throw OpexError.BadRequest.exception()
    }

    override fun getQuoteCurrencies(): List<QuoteCurrency> {
        val uri = UriComponentsBuilder.fromUriString("$baseUrl/currency/quotes")
//            .queryParam("isReference", true)
            .build().toUri()
        return restTemplate.exchange<Array<QuoteCurrency>>(uri, HttpMethod.GET, noBody()).body?.toList() ?: emptyList()
    }

    override fun getSwapTransactions(token: String, request: UserTransactionRequest): List<SwapResponse> {
        return restTemplate.exchange<Array<SwapResponse>>(
            "$baseUrl/v1/swap/history",
            HttpMethod.POST,
            body(request, token)
        ).body?.toList() ?: emptyList()
    }

    override fun getSwapTransactionsCount(
        token: String,
        request: UserTransactionRequest
    ): Long {
        return restTemplate.exchange<Long?>(
            "$baseUrl/v1/swap/history/count",
            HttpMethod.POST,
            body(request, token)
        ).body ?: 0
    }
}

