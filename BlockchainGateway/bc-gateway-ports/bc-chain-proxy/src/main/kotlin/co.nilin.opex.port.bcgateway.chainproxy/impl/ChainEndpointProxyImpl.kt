package co.nilin.opex.port.bcgateway.chainproxy.impl

import co.nilin.opex.bcgateway.core.model.ChainSyncRecord
import co.nilin.opex.bcgateway.core.model.Endpoint
import co.nilin.opex.bcgateway.core.spi.ChainEndpointProxy
import org.springframework.stereotype.Component

class ChainEndpointProxyImpl(private val endpoints: List<Endpoint>) : ChainEndpointProxy {
    override suspend fun syncTransfers(filter: ChainEndpointProxy.DepositFilter): ChainSyncRecord {
        TODO("Not yet implemented")
    }
}
