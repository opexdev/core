package co.nilin.opex.api.ports.proxy.impl

import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.CurrencyImplementation
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.core.utils.LoggerDelegate
import co.nilin.opex.api.ports.proxy.data.AssignAddressRequest
import co.nilin.opex.api.ports.proxy.data.DepositDetailsRequest
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Mono
import java.net.URI

@Component
class BlockchainGatewayProxyImpl(private val client: WebClient) : BlockchainGatewayProxy {

    private val logger by LoggerDelegate()

    @Value("\${app.opex-bc-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun assignAddress(uuid: String, currency: String): AssignResponse? {
        logger.info("calling bc-gateway assign")
        return client.post()
            .uri(URI.create("$baseUrl/address/assign"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(AssignAddressRequest(uuid, currency)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(AssignResponse::class.java)
            .awaitSingleOrNull()
    }

    override suspend fun getDepositDetails(refs: List<String>): List<DepositDetails> {
        logger.info("calling bc-gateway deposit details")
        return client.post()
            .uri(URI.create("$baseUrl/deposit/find/all"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(DepositDetailsRequest(refs)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<DepositDetails>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    override suspend fun getCurrencyImplementations(currency: String?): List<CurrencyImplementation> {
        logger.info("calling bc-gateway chain details")
        return client.get()
            .uri("$baseUrl/currency/chains") {
                it.queryParam("currency", currency)
                it.build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux<CurrencyImplementation>()
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }
}