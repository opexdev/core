package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.pricemanagement.PairRateConfigView
import co.nilin.opex.api.core.inout.pricemanagement.ProviderPrice
import co.nilin.opex.api.core.inout.pricemanagement.UpsertPairRateConfigRequest
import co.nilin.opex.api.core.spi.PriceManagementProxy
import co.nilin.opex.api.ports.proxy.config.ProxyDispatchers
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
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
class PriceManagementProxyImpl(@Qualifier("generalWebClient") private val webClient: WebClient) : PriceManagementProxy {

    @Value("\${app.price-management.url}")
    private lateinit var baseUrl: String

    override suspend fun getConfigs(token: String): List<PairRateConfigView> {
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                    .uri("$baseUrl/admin/rate-config")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToFlux<PairRateConfigView>()
                    .collectList()
                    .awaitSingle()
        }
    }

    override suspend fun getConfig(token: String, symbol: String): PairRateConfigView {
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                    .uri("$baseUrl/admin/rate-config/$symbol")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono<PairRateConfigView>()
                    .awaitSingle()
        }
    }

    override suspend fun upsertConfig(token: String, request: UpsertPairRateConfigRequest): PairRateConfigView {
        return withContext(ProxyDispatchers.general) {
            webClient.post()
                    .uri("$baseUrl/admin/rate-config")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request))
                    .retrieve()
                    .bodyToMono<PairRateConfigView>()
                    .awaitSingle()
        }
    }

    override suspend fun getProvidersPrice(token: String, symbol: String): List<ProviderPrice> {
        return withContext(ProxyDispatchers.general) {
            webClient.get()
                    .uri("$baseUrl/admin/rate-config/$symbol/providers")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToFlux<ProviderPrice>()
                    .collectList()
                    .awaitSingle()
        }
    }
}
