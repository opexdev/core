package co.nilin.opex.api.ports.binance.proxy

import co.nilin.opex.api.core.inout.AssignResponse
import co.nilin.opex.api.core.inout.DepositDetails
import co.nilin.opex.api.core.spi.BlockchainGatewayProxy
import co.nilin.opex.api.ports.binance.util.LoggerDelegate
import kotlinx.coroutines.reactive.awaitSingleOrElse
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import reactor.core.publisher.Mono
import java.net.URI

private inline fun <reified T : Any?> typeRef(): ParameterizedTypeReference<T> =
    object : ParameterizedTypeReference<T>() {}

@Component
class BlockchainGatewayProxyImpl(private val client: WebClient) : BlockchainGatewayProxy {

    private data class AssignAddressRequest(
        val uuid: String,
        val currency: String
    )

    private data class DepositDetailsRequest(val refs: List<String>)

    private val logger by LoggerDelegate()

    @Value("\${app.opex-bc-gateway.url}")
    private lateinit var baseUrl: String

    override suspend fun assignAddress(uuid: String, currency: String): AssignResponse {
        logger.info("calling bc-gateway assign")
        return client.post()
            .uri(URI.create("$baseUrl/address/assign"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(AssignAddressRequest(uuid, currency)))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<AssignResponse>())
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
            .bodyToMono(typeRef<List<DepositDetails>>())
            .awaitSingleOrElse { emptyList() }
    }
}