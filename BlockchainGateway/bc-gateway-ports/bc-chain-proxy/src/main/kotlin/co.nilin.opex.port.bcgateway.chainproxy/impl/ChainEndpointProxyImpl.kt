package co.nilin.opex.port.bcgateway.chainproxy.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.Deposit
import co.nilin.opex.bcgateway.core.model.DepositResult
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.body
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDateTime

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

class ChainEndpointProxyImpl(
    private val chain: String,
    private val endpoints: List<Endpoint>,
    private val webClient: WebClient
) : ChainEndpointProxy {

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

    data class TransferResponse(
        val latestBlock: Long,
        val transfers: List<Transfer>
    )

    private val logger = LoggerFactory.getLogger(ChainEndpointProxyImpl::class.java)

    private suspend fun requestTransferList(endpoint: String, request: TransfersRequest): DepositResult {
        logger.info("request transfers: base=$endpoint")
        val response = webClient.post()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .body(Mono.just(request))
            .retrieve()
            .onStatus({ t -> t.isError }, { it.createException() })
            .bodyToMono(typeRef<TransferResponse>())
            .awaitFirstOrNull()

        return DepositResult(
            response?.latestBlock ?: request.startBlock ?: 0,
            response?.transfers
                ?.map { Deposit(null, it.txHash, it.to, null, it.amount, chain, it.isTokenTransfer, it.token) }
                ?: emptyList()
        )
    }

    private suspend fun roundRobin(i: Int, request: TransfersRequest): ChainSyncRecord {
        return try {
            val response =
                requestTransferList(
                    endpoints[i].url,
                    request
                )
            logger.info("fetched transactions: ${response.deposits.size} transaction received")
            ChainSyncRecord(
                chain,
                LocalDateTime.now(),
                endpoints[i],
                response.latestBlock,
                true,
                null,
                response.deposits
            )
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
