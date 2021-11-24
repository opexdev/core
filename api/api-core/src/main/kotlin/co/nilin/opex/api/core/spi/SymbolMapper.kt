package co.nilin.opex.api.core.spi

interface SymbolMapper {

    suspend fun map(symbol: String?): String?

    suspend fun unmap(value: String?): String?

    suspend fun getKeyValues(): Map<String, String>
}