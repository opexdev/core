package co.nilin.opex.price.core.spi

interface PairProviderIncludePersister {
    suspend fun replaceIncludedProviders(symbol: String, providers: List<String>)
}
