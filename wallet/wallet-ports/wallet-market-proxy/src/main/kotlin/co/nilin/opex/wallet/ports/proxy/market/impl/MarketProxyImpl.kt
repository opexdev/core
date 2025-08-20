package co.nilin.opex.wallet.ports.proxy.market.impl

import co.nilin.opex.wallet.core.inout.PriceTicker
import co.nilin.opex.wallet.core.spi.MarketProxy
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.net.URI

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Component
class MarketProxyImpl(private val webClient: WebClient) : MarketProxy {

    @Value("\${app.market.url}")
    private lateinit var baseUrl: String

    override suspend fun fetchPrices(symbol: String?): List<PriceTicker> {
        val uri = if (!symbol.isNullOrBlank()) {
            URI.create("$baseUrl/v1/market/prices?symbol=$symbol")
        } else {
            URI.create("$baseUrl/v1/market/prices")
        }
        return webClient.get()
            .uri(uri)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<List<PriceTicker>>())
            .awaitFirst()
    }
}
