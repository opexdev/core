package co.nilin.opex.price.core.spi

interface PairProviderIncludePersister {
    /** Replaces the full set of included providers for [symbol] with [providers]. */
    suspend fun replaceIncludedProviders(symbol: String, providers: List<String>)
}
