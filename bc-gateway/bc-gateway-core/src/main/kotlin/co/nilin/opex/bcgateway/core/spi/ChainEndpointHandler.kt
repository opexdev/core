package co.nilin.opex.bcgateway.core.spi

interface ChainEndpointHandler {

    suspend fun addEndpoint(chainName: String, url: String, username: String?, password: String?)

    suspend fun deleteEndpoint(chainName: String, url: String)

    suspend fun findChainEndpointProxy(chainName: String): ChainEndpointProxy
}
