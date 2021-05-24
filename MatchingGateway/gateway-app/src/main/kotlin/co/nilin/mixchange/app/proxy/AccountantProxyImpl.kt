package co.nilin.mixchange.port.gateway.wallet.proxy


import co.nilin.mixchange.app.inout.PairFeeConfig
import co.nilin.mixchange.app.spi.AccountantApiProxy
import co.nilin.mixchange.matching.core.model.OrderDirection
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class AccountantProxyImpl(
    @Value("\${app.accountant.url}") val accountantBaseUrl: String, val webClient: WebClient
) : AccountantApiProxy {
    override suspend fun canCreateOrder(uuid: String, symbol: String, value: BigDecimal): Boolean {
        data class BooleanResponse(val result: Boolean)
        return webClient.get()
            .uri(URI.create("$accountantBaseUrl/$uuid/create_order/${value}_${symbol}/allowed"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { p ->
                throw RuntimeException()
            })
            .bodyToMono(typeRef<BooleanResponse>())
            .log()
            .awaitFirst()
            .result
    }

    override suspend fun fetchPairFeeConfig(pair: String, direction: OrderDirection, userLevel: String): PairFeeConfig {
        return webClient.get()
            .uri(
                URI.create(
                    if (userLevel.isBlank()) {
                        "$accountantBaseUrl/config/${pair}/fee/${direction}"
                    } else {
                        "$accountantBaseUrl/config/${pair}/fee/${direction}-${userLevel}"
                    }
                )
            )
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { p ->
                throw RuntimeException()
            })
            .bodyToMono(typeRef<PairFeeConfig>())
            .log()
            .awaitFirst()
    }
}