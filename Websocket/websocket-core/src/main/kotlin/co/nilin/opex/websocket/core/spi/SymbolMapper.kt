package co.nilin.opex.websocket.core.spi

interface SymbolMapper {

    suspend fun map(symbol: String?): String?

    suspend fun unmap(value: String?): String?

    suspend fun getKeyValues(): Map<String, String>
}