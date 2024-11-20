package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.OwnerLimitsResponse
import co.nilin.opex.api.core.inout.TransactionHistoryResponse
import co.nilin.opex.api.core.inout.Wallet
import co.nilin.opex.api.core.inout.WithdrawHistoryResponse
import co.nilin.opex.api.core.spi.WalletProxy
import co.nilin.opex.api.ports.proxy.data.TransactionRequest
import co.nilin.opex.common.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
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
        return webClient.get()
            .uri("$baseUrl/v1/owner/$uuid/wallets")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<Wallet>()
            .collectList()
            .awaitSingle()
    }

    override suspend fun getWallet(uuid: String?, token: String?, symbol: String): Wallet {
        logger.info("fetching wallets for $uuid")
        return webClient.get()
            .uri("$baseUrl/v1/owner/$uuid/wallets/$symbol")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<Wallet>()
            .awaitSingle()
    }

    override suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return webClient.get()
            .uri("$baseUrl/v1/owner/$uuid/limits")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<OwnerLimitsResponse>()
            .awaitSingle()
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
        return webClient.post()
            .uri("$baseUrl/transaction/deposit/$uuid")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(TransactionRequest(coin, startTime, endTime, limit, offset, ascendingByTime)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<TransactionHistoryResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
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
        return webClient.post()
            .uri("$baseUrl/withdraw/history/$uuid")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .body(Mono.just(TransactionRequest(coin, startTime, endTime, limit, offset, ascendingByTime)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<WithdrawHistoryResponse>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }


}