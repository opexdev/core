package co.nilin.opex.api.ports.postgres.impl

import co.nilin.opex.api.core.spi.SymbolMapper
import co.nilin.opex.api.ports.postgres.dao.SymbolMapRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component
class SymbolMapperImpl(val symbolMapRepository: SymbolMapRepository) : SymbolMapper {

    private var symbolsCache: Map<String, String>? = null

    override fun fromInternalSymbol(symbol: String?): String? {
        if (symbol == null) return null
        return symbolMapRepository.findByAliasKeyAndSymbol("binance", symbol)?.alias
    }

    override fun toInternalSymbol(alias: String?): String? {
        if (alias == null) return null
        return symbolMapRepository.findByAliasKeyAndAlias("binance", alias)?.symbol
    }

    override fun symbolToAliasMap(): Map<String, String> {
        if (symbolsCache.isNullOrEmpty()) {
            symbolsCache = symbolMapRepository.findAllByAliasKey("binance").associate { it.symbol to it.alias }
        }
        return symbolsCache!!
    }
}
