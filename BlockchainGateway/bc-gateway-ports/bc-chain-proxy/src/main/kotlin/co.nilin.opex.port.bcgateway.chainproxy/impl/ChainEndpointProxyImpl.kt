package co.nilin.opex.port.bcgateway.chainproxy.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

class ChainEndpointProxyImpl(
    private val chain: String,
    private val endpoints: List<Endpoint>,
    private val webClient: WebClient
) :
    ChainEndpointProxy {
    data class TransfersRequest(
        val startBlock: Long?,
        val endBlock: Long?,
        val addresses: List<String>?
    )

    data class Transfer(
        var txHash: String,
        var from: String,
        var to: String,
        var isTokenTransfer: Boolean,
        var token: String? = null,
        var amount: BigDecimal
    )

    private suspend fun requestTransferList(baseUrl: String, request: TransfersRequest): List<Deposit> {
        return webClient.post()
            .uri(URI.create("$baseUrl/transfers"))
            .header("Content-Type", "application/json")
            .body(Mono.just(request), TransfersRequest::class.java)
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToFlux(typeRef<Transfer>())
            .log().map { Deposit(null, it.to, null, it.amount, chain, it.isTokenTransfer, it.token) }
            .collectList()
            .awaitFirstOrElse { emptyList() }
    }

    private suspend fun roundRobin(i: Int, request: TransfersRequest): ChainSyncRecord {
        return try {
            val deposits =
                requestTransferList(
                    endpoints[i].url,
                    request
                )
            ChainSyncRecord(chain, LocalDateTime.now(), endpoints[i], request.endBlock, true, null, deposits)
        } catch (error: WebClientResponseException) {
            if (i < endpoints.size - 1) {
                roundRobin(i + 1, request)
            } else {
                ChainSyncRecord(
                    chain,
                    LocalDateTime.now(),
                    endpoints[i],
                    request.endBlock,
                    false,
                    error.message,
                    emptyList()
                )
            }
        }
    }

    override suspend fun syncTransfers(filter: ChainEndpointProxy.DepositFilter): ChainSyncRecord {
        return roundRobin(0, TransfersRequest(filter.startBlock, filter.endBlock, filter.tokenAddresses))
    }
}
