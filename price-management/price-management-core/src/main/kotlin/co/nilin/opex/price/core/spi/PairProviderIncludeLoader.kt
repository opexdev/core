package co.nilin.opex.price.core.spi

import co.nilin.opex.price.core.dto.PairProviderInclude

interface PairProviderIncludeLoader {
    suspend fun loadIncludedProviders(symbol: String): List<PairProviderInclude>
}
