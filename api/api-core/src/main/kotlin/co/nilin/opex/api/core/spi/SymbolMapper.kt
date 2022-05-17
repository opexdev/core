package co.nilin.opex.api.core.spi

interface SymbolMapper {

    suspend fun map(symbol: String?): String?

    suspend fun unmap(alias: String?): String?

    suspend fun symbolToAliasMap(): Map<String, String>
}
