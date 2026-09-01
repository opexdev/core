package co.nilin.opex.price.ports.wallet.impl

import co.nilin.opex.price.core.spi.WalletRateProxy
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@Component
class WalletRateProxyImpl(
    private val walletWebClient: WebClient
) : WalletRateProxy {

    private data class UpsertRateBody(val sourceSymbol: String, val destSymbol: String, val rate: BigDecimal)

    override suspend fun upsertRate(sourceSymbol: String, destSymbol: String, rate: BigDecimal) {
        walletWebClient.post()
            .uri("/internal/otc/rate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(UpsertRateBody(sourceSymbol, destSymbol, rate))
            .retrieve()
            .onStatus({ it.isError }, { it.createException() })
            .toBodilessEntity()
            .awaitFirstOrNull()
    }
}
