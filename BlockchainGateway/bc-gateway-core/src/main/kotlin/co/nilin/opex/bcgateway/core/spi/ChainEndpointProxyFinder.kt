package co.nilin.opex.bcgateway.core.spi

interface ChainEndpointProxyFinder {
    suspend fun findChainEndpointProxy(chainName: String): ChainEndpointProxy
}
