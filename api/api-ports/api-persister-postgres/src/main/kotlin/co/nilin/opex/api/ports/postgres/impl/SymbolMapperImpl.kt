package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.SymbolMapRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class SymbolMapperImpl(val symbolMapRepository: SymbolMapRepository) : SymbolMapper {

    override suspend fun map(symbol: String?): String? {
        if (symbol == null) return null
        return symbolMapRepository.findBySymbol(symbol).awaitFirstOrNull()?.value
    }

    override suspend fun unmap(value: String?): String? {
        if (value == null) return null
        return symbolMapRepository.findByValue(value).awaitFirstOrNull()?.symbol
    }

    override suspend fun getKeyValues(): Map<String, String> {
        val map = HashMap<String, String>()
        symbolMapRepository.findAll()
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .forEach { map[it.symbol] = it.value }
        return map
    }
}