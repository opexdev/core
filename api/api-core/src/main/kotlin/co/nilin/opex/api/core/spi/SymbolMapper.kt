package co.nilin.opex.api.core.spi

interface SymbolMapper {

    suspend fun fromInternalSymbol(symbol: String?): String?

    suspend fun toInternalSymbol(alias: String?): String?

    suspend fun symbolToAliasMap(): Map<String, String>
}
