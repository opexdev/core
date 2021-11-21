package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.Chain

interface ChainLoader {
    suspend fun fetchChainInfo(chain: String): Chain
}
