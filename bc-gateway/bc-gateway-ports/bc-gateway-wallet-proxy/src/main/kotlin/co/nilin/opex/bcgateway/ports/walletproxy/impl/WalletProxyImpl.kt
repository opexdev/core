package co.nilin.opex.bcgateway.ports.walletproxy.impl

import co.nilin.opex.bcgateway.core.spi.WalletProxy
import co.nilin.opex.bcgateway.ports.walletproxy.model.TransferResult
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class WalletProxyImpl(private val webClient: WebClient,
                      private val extractBackgroundAuth: ExtractBackgroundAuth) : WalletProxy {

    @Value("\${app.wallet.url}")
    private lateinit var baseUrl: String

//    override suspend fun transfer(uuid: String, symbol: String, amount: BigDecimal, hash: String) {
//        webClient.post()
//                .uri(URI.create("$baseUrl/deposit/${amount}_${symbol}/${uuid}_main?transferRef=$hash"))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer ${extractBackgroundAuth.extractToken()}")
//                .retrieve()
//                .onStatus({ t -> t.isError }, { it.createException() })
//                .bodyToMono(typeRef<TransferResult>())
//                .log()
//                .awaitFirst()
//    }


    override suspend fun transfer(uuid: String, symbol: String, amount: BigDecimal, hash: String, chain: String) {

        val token = extractBackgroundAuth.extractToken()
        webClient.post()
                .uri(URI.create("$baseUrl/deposit/${amount}_${chain}_${symbol}/${uuid}_main?transferRef=" +
                        "" +
                        "" +
                        "$hash"))
                .headers { httpHeaders ->
                    run {
                        httpHeaders.add("Content-Type", "application/json");
                        token?.let { httpHeaders.add("Authorization", "Bearer $it") }
                    }
                }
                .retrieve()
                .onStatus({ t -> t.isError }, { it.createException() })
                .bodyToMono(typeRef<TransferResult>())
                .log()
                .awaitFirst()
    }
}
