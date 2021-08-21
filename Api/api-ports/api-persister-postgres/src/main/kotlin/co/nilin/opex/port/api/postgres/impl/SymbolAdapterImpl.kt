package co.nilin.opex.port.api.postgres.impl

import co.nilin.opex.api.core.spi.SymbolAdapter
import co.nilin.opex.port.api.postgres.dao.PairMapRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class SymbolAdapterImpl(val pairMapRepository: PairMapRepository) : SymbolAdapter {
    override suspend fun mapTo(pair: String?): String? {
        if (pair == null) return null
        return pairMapRepository.findByPair(pair).awaitFirstOrNull()?.map
    }

    override suspend fun mapFrom(map: String?): String? {
        if (map == null) return null
        return pairMapRepository.findByMap(map).awaitFirstOrNull()?.pair
    }
}