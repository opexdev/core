package co.nilin.opex.bcgateway.core.spi

interface ChainEndpointHandler {

    suspend fun addEndpoint(chainName: String, endpoint: String)

    suspend fun findChainEndpointProxy(chainName: String): ChainEndpointProxy
}
