package co.nilin.opex.bcgateway.core.spi

import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo

interface CurrencyLoader {
    suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo
    suspend fun findSymbol(chain: String, address: String?): String
    suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation>
}