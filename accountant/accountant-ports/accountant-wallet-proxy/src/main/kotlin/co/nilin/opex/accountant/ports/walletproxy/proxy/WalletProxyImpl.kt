package co.nilin.opex.accountant.ports.walletproxy.proxy

import co.nilin.opex.accountant.core.model.CurrencyPrice
import co.nilin.opex.accountant.core.model.TotalAssetsSnapshot
import co.nilin.opex.accountant.core.model.WalletType
import co.nilin.opex.accountant.core.spi.WalletProxy
import co.nilin.opex.accountant.ports.walletproxy.data.BooleanResponse
import co.nilin.opex.accountant.ports.walletproxy.data.TransferResult
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.io.IOException
import java.net.ConnectException
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component
class WalletProxyImpl(
    private val webClient: WebClient,
    @Value("\${app.wallet.url}") private val walletBaseUrl: String,
    @Value("\${app.wallet.http.retry.count:2}") private val retryCount: Long = 2,
    @Value("\${app.wallet.http.retry.delay-millis:250}") private val retryDelayMillis: Long = 250,
    @Value("\${app.wallet.http.timeout-seconds:10}") private val timeoutSeconds: Long = 10
) : WalletProxy {

    data class TransferBody(
        val description: String?,
        val transferRef: String?,
        val transferCategory: String
    )

    override suspend fun transfer(
        symbol: String,
        senderWalletType: WalletType,
        senderUuid: String,
        receiverWalletType: WalletType,
        receiverUuid: String,
        amount: BigDecimal,
        description: String?,
        transferRef: String?,
        transferCategory: String
    ) {
        withTransientRetry {
            webClient.post()
                .uri("$walletBaseUrl/v2/transfer/${amount}_$symbol/from/${senderUuid}_$senderWalletType/to/${receiverUuid}_$receiverWalletType")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TransferBody(description, transferRef, transferCategory))
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono<TransferResult>()
        }.awaitFirst()
    }

    override suspend fun canFulfil(symbol: String, walletType: WalletType, uuid: String, amount: BigDecimal): Boolean {
        return webClient.get()
            .uri("$walletBaseUrl/inquiry/$uuid/wallet_type/$walletType/can_withdraw/${amount}_$symbol")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<BooleanResponse>()
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .awaitFirst()
            .result
    }

    override suspend fun getUserTotalAssets(
        uuid: String,
    ): TotalAssetsSnapshot? {
        return webClient.get()
            .uri("$walletBaseUrl/stats/total-assets/$uuid")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<TotalAssetsSnapshot>()
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .awaitFirstOrNull()
    }

    override suspend fun getPrices(quote: String): List<CurrencyPrice> {
        return webClient.get()
            .uri("$walletBaseUrl/otc/currency/price?unit=$quote")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono<List<CurrencyPrice>>()
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .awaitFirst()
    }

    private fun <T> withTransientRetry(request: () -> Mono<T>): Mono<T> {
        return request()
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .retryWhen(
                Retry.backoff(retryCount, Duration.ofMillis(retryDelayMillis))
                    .filter { error ->
                        when {
                            error is WebClientRequestException -> true
                            error is TimeoutException -> true
                            error is ConnectException -> true
                            error is IOException -> true
                            error.cause is TimeoutException -> true
                            error.cause is ConnectException -> true
                            else -> false
                        }
                    }
                    .onRetryExhaustedThrow { _, signal -> signal.failure() }
            )
    }
}