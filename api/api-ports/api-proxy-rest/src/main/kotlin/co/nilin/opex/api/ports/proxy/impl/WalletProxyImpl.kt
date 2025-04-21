package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.inout.TransactionHistoryResponse
import co.nilin.opex.api.core.inout.Wallet
import co.nilin.opex.api.core.inout.WithdrawHistoryResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class WalletProxyImpl(private val restTemplate: RestTemplate) : WalletProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override suspend fun getWallets(uuid: String?, token: String?): List<Wallet> {
        logger.info("fetching wallets for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/owner/$uuid/wallets")
                .build()
                .toUri()

            val request = RequestEntity.get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            restTemplate.exchange(
                request,
                Array<Wallet>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet {
        logger.info("fetching wallet for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/owner/$uuid/wallets/$symbol")
                .build()
                .toUri()

            val request = RequestEntity.get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            restTemplate.exchange(
                request,
                Wallet::class.java
            ).body ?: throw RuntimeException("Failed to get wallet for $uuid")
        }
    }

    override suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/v1/owner/$uuid/limits")
                .build()
                .toUri()

            val request = RequestEntity.get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .build()

            restTemplate.exchange(
                request,
                OwnerLimitsResponse::class.java
            ).body ?: throw RuntimeException("Failed to get owner limits for $uuid")
        }
    }

    override suspend fun getDepositTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<TransactionHistoryResponse> {
        logger.info("fetching deposit transaction history for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/transaction/deposit/$uuid")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(TransactionRequest(coin, startTime, endTime, limit, offset, ascendingByTime))

            restTemplate.exchange(
                request,
                Array<TransactionHistoryResponse>::class.java
            ).body?.toList() ?: emptyList()
        }
    }

    override suspend fun getWithdrawTransactions(
        uuid: String,
        token: String?,
        coin: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int,
        ascendingByTime: Boolean?
    ): List<WithdrawHistoryResponse> {
        logger.info("fetching withdraw transaction history for $uuid")
        return withContext(ProxyDispatchers.wallet) {
            val uri = UriComponentsBuilder.fromUriString("$baseUrl/withdraw/history/$uuid")
                .build()
                .toUri()

            val request = RequestEntity.post(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .body(TransactionRequest(coin, startTime, endTime, limit, offset, ascendingByTime))

            restTemplate.exchange(
                request,
                Array<WithdrawHistoryResponse>::class.java
            ).body?.toList() ?: emptyList()
        }
    }
}