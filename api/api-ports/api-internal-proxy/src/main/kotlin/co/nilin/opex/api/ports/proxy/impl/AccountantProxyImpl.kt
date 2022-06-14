package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.PairFeeResponse
import co.nilin.opex.api.core.inout.PairInfoResponse
import co.nilin.opex.api.core.spi.AccountantProxy
import co.nilin.opex.api.core.utils.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux

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
            .bodyToFlux<PairInfoResponse>()
            .collectList()
            .awaitSingle()
    }

    override suspend fun getFeeConfigs(): List<PairFeeResponse> {
        logger.info("fetching fee configs")
        return webClient.get()
            .uri("$baseUrl/config/fee/all")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<PairFeeResponse>()
            .collectList()
            .awaitSingle()
    }
}