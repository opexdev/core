package co.nilin.opex.referral.ports.api.proxy.proxy

import co.nilin.opex.referral.core.spi.ApiProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal
import java.net.URI

data class PriceTickerResponse(
    val symbol: String?,
    val price: String?
)

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class ApiProxyImpl(
    @Value("\${app.api.url}") val apiBaseUrl: String, val webClient: WebClient
) : ApiProxy {
    override suspend fun fetchLastPrice(pairSymbol: String): BigDecimal? {
        val list = webClient.get()
            .uri(URI.create("$apiBaseUrl/v3/ticker/price?symbol=${pairSymbol.uppercase()}"))
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<List<PriceTickerResponse>>())
            .log()
            .awaitFirst()
        return list.first().price?.let { BigDecimal(it) }
    }
}
