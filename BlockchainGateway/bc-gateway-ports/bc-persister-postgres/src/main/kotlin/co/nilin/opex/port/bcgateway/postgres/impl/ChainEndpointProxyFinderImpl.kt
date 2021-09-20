package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxyFinder
import co.nilin.opex.port.bcgateway.chainproxy.impl.ChainEndpointProxyImpl
import co.nilin.opex.port.bcgateway.postgres.dao.ChainRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ChainEndpointProxyFinderImpl(private val chainRepository: ChainRepository) : ChainEndpointProxyFinder {
    override suspend fun findChainEndpointProxy(chainName: String): ChainEndpointProxy {
        val endpoints = chainRepository.findEndpointsByName(chainName).map { Endpoint(it.url) }.toList()
        return ChainEndpointProxyImpl(endpoints)
    }
}
