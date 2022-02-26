package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointHandler
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import co.nilin.opex.bcgateway.ports.chainproxy.impl.ChainEndpointProxyImpl
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainEndpointRepository
import co.nilin.opex.bcgateway.ports.postgres.dao.ChainRepository
import co.nilin.opex.bcgateway.ports.postgres.model.ChainEndpointModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class ChainEndpointHandlerImpl(
    private val webClient: WebClient,
    private val chainRepository: ChainRepository,
    private val endpointRepository: ChainEndpointRepository
) : ChainEndpointHandler {

    override suspend fun addEndpoint(chainName: String, url: String, username: String?, password: String?) {
        endpointRepository.save(ChainEndpointModel(null, chainName, url, username, password)).awaitFirstOrNull()
    }

    override suspend fun deleteEndpoint(chainName: String, url: String) {
        endpointRepository.deleteByChainNameAndUrl(chainName, url).awaitFirstOrNull()
    }

    override suspend fun findChainEndpointProxy(chainName: String): ChainEndpointProxy {
        val endpoints = chainRepository.findEndpointsByName(chainName).map { Endpoint(it.url) }.toList()
        return ChainEndpointProxyImpl(chainName, endpoints, webClient)
    }
}
