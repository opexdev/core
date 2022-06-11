package co.nilin.opex.market.core.spi

interface SymbolMapper {

    suspend fun map(symbol: String?): String?

    suspend fun unmap(alias: String?): String?

    suspend fun symbolToAliasMap(): Map<String, String>
}
