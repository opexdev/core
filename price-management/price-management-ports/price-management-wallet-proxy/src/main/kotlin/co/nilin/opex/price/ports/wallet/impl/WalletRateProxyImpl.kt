package co.nilin.opex.price.ports.wallet.impl

import co.nilin.opex.price.core.spi.Notifier
import co.nilin.opex.price.core.spi.WalletRateProxy
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class WalletRateProxyImpl(
    private val walletWebClient: WebClient,
    private val notifier: Notifier,
) : WalletRateProxy {

    private val logger = LoggerFactory.getLogger(WalletRateProxyImpl::class.java)

    private data class UpsertRateBody(val sourceSymbol: String, val destSymbol: String, val rate: BigDecimal)

    override suspend fun upsertRate(sourceSymbol: String, destSymbol: String, rate: BigDecimal) {
        val response = walletWebClient.post()
            .uri("/internal/otc/rate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpsertRateBody(sourceSymbol, destSymbol, rate))
            .exchangeToMono { it.toEntity(String::class.java) }
            .awaitFirstOrNull()

        val statusCode = response?.statusCode?.value()
        if (statusCode != 200) {
            val detail = "status=${statusCode ?: "no response"}, body=${response?.body ?: "-"}"
            logger.error("Wallet OTC rate upsert failed for $sourceSymbol-$destSymbol: $detail")
            notifier.notify(
                buildString {
                    appendLine("⚠️ Wallet OTC rate upsert failed")
                    appendLine("Pair: $sourceSymbol-$destSymbol")
                    appendLine("Rate: $rate")
                    append(detail)
                }
            )
            error("Wallet OTC rate upsert returned $detail")
        }
    }
}
