package co.nilin.opex.api.core.spi

interface SymbolMapper {

    fun fromInternalSymbol(symbol: String?): String?

    fun toInternalSymbol(alias: String?): String?

    fun symbolToAliasMap(): Map<String, String>
}
