package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.PairProviderInclude
import co.nilin.opex.price.core.spi.PairProviderIncludeLoader
import co.nilin.opex.price.ports.postgres.dao.PairProviderIncludeRepository
import co.nilin.opex.price.ports.postgres.utils.asCoreModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class PairProviderIncludeLoaderImpl(
    private val pairProviderIncludeRepository: PairProviderIncludeRepository
) : PairProviderIncludeLoader {

    override suspend fun loadIncludedProviders(symbol: String): List<PairProviderInclude> {
        return pairProviderIncludeRepository.findBySymbol(symbol)
            .collectList()
            .awaitFirstOrNull()
            ?.map { it.asCoreModel() }
            ?: emptyList()
    }
}
