package co.nilin.opex.referral.ports.accountant.proxy.proxy

import co.nilin.opex.accountant.core.model.PairConfig
import co.nilin.opex.referral.core.spi.AccountantProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class AccountantProxyImpl(
    @Value("\${app.accountant.url}") val accountantBaseUrl: String, val webClient: WebClient
) : AccountantProxy {
    override suspend fun fetchPairConfigs(): List<PairConfig> {
        return webClient.get()
            .uri(URI.create("$accountantBaseUrl/config/all"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<List<PairConfig>>())
            .log()
            .awaitFirst()
    }
}
