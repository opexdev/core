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
        return symbolMapRepository.findByAliasKeyAndSymbol("binance", symbol).awaitFirstOrNull()?.alias
    }

    override suspend fun unmap(alias: String?): String? {
        if (alias == null) return null
        return symbolMapRepository.findByAliasKeyAndAlias("binance", alias).awaitFirstOrNull()?.symbol
    }

    override suspend fun symbolToAliasMap(): Map<String, String> {
        return symbolMapRepository.findAll()
            .filter { it.aliasKey == "binance" }
            .collectList()
            .awaitFirstOrElse { emptyList() }
            .associate { it.symbol to it.alias }
    }
}
