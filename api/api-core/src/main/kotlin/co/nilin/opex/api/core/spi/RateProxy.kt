package co.nilin.opex.api.core.spi

import co.nilin.opex.api.core.inout.otc.*

interface RateProxy {
    // Rates (writes require admin token)
    suspend fun createRate(token: String, request: SetCurrencyExchangeRateRequest)
    suspend fun updateRate(token: String, request: SetCurrencyExchangeRateRequest): Rates
    suspend fun deleteRate(token: String, sourceSymbol: String, destSymbol: String): Rates

    // Rates (reads are public)
    suspend fun fetchRates(): Rates
    suspend fun fetchRate(sourceSymbol: String, destSymbol: String): Rate?

    // Forbidden pairs
    suspend fun addForbiddenPair(token: String, request: CurrencyPair)
    suspend fun deleteForbiddenPair(token: String, sourceSymbol: String, destSymbol: String): ForbiddenPairs

    // Forbidden pairs (read is public)
    suspend fun fetchForbiddenPairs(): ForbiddenPairs

    // Transitive symbols
    suspend fun addTransitiveSymbols(token: String, symbols: Symbols)
    suspend fun deleteTransitiveSymbol(token: String, symbol: String): Symbols
    suspend fun deleteTransitiveSymbols(token: String, symbols: Symbols): Symbols

    // Transitive symbols (read is public)
    suspend fun fetchTransitiveSymbols(): Symbols

    // Routes and prices (reads are public)
    suspend fun fetchRoutes(sourceSymbol: String? = null, destSymbol: String? = null): CurrencyExchangeRatesResponse
    suspend fun getPrice(unit: String): List<CurrencyPrice>
}
