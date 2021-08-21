package co.nilin.opex.api.core.spi

interface SymbolAdapter {
    suspend fun mapTo(pair: String?): String?
    suspend fun mapFrom(map: String?): String?
}