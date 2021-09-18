package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.Chain
import co.nilin.opex.bcgateway.core.model.CurrencyInfo

interface ChainLoader {
    suspend fun fetchChainInfo(chain: String): Chain
}