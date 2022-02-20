package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo

interface CurrencyLoader {

    suspend fun addCurrency(name: String, symbol: String)

    suspend fun editCurrency(name: String, symbol: String)

    suspend fun deleteCurrency(name: String)

    suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo

    suspend fun findByChainAndTokenAddress(chain: String, address: String?): CurrencyImplementation?

    suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation>

}