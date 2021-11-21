package co.nilin.opex.api.ports.binance.proxy

import co.nilin.opex.api.core.inout.PairInfoResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.ports.binance.util.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

private inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> =
    object : ParameterizedTypeReference<T>() {}

@Component
class AccountantProxyImpl(private val webClient: WebClient) : AccountantProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.accountant.url}")
    private lateinit var baseUrl: String

    override suspend fun getPairConfigs(): List<PairInfoResponse> {
        logger.info("fetching pair configs")
        return webClient.get()
            .uri("$baseUrl/config/all")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux(typeRef<PairInfoResponse>())
            .collectList()
            .awaitSingle()
    }
}