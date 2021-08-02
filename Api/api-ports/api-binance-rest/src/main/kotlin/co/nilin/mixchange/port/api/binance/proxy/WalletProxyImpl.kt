package co.nilin.mixchange.port.api.binance.proxy

import co.nilin.mixchange.api.core.inout.OwnerLimitsResponse
import co.nilin.mixchange.api.core.inout.Wallet
import co.nilin.mixchange.api.core.spi.WalletProxy
import co.nilin.mixchange.port.api.binance.util.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.time.LocalDateTime

private inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> =
    object : ParameterizedTypeReference<T>() {}

@Component
class WalletProxyImpl(private val webClient: WebClient) : WalletProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

    override suspend fun getWallets(uuid: String?, token: String?): List<Wallet> {
        logger.info("fetching wallets for $uuid")
        return webClient.get()
            .uri("$baseUrl/owner/wallet/all")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { throw RuntimeException() })
            .bodyToFlux(typeRef<Wallet>())
            .collectList()
            .awaitSingle()
    }

    override suspend fun getOwnerLimits(uuid: String?, token: String?): OwnerLimitsResponse {
        logger.info("fetching owner limits for $uuid")
        return webClient.get()
            .uri("$baseUrl/owner/limits")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .onStatus({ t -> t.isError }, { throw RuntimeException() })
            .bodyToMono(typeRef<OwnerLimitsResponse>())
            .awaitSingle()
    }
}