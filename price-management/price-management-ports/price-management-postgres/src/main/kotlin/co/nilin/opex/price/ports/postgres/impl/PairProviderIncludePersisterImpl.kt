package co.nilin.opex.price.ports.postgres.impl

import co.nilin.opex.price.core.dto.PairProviderInclude
import co.nilin.opex.price.core.spi.PairProviderIncludePersister
import co.nilin.opex.price.ports.postgres.dao.PairProviderIncludeRepository
import co.nilin.opex.price.ports.postgres.utils.asModel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class PairProviderIncludePersisterImpl(
    private val pairProviderIncludeRepository: PairProviderIncludeRepository
) : PairProviderIncludePersister {

    override suspend fun replaceIncludedProviders(symbol: String, providers: List<String>) {
        pairProviderIncludeRepository.deleteBySymbol(symbol).awaitFirstOrNull()
        if (providers.isEmpty()) return

        val models = providers.distinct().map { provider -> PairProviderInclude(symbol, provider).asModel() }
        pairProviderIncludeRepository.saveAll(models).collectList().awaitFirst()
    }
}
