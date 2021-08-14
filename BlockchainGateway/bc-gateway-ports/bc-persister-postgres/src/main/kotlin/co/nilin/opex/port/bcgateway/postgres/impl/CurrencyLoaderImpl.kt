package co.nilin.opex.port.bcgateway.postgres.impl

import co.nilin.opex.bcgateway.core.model.CurrencyImplementation
import co.nilin.opex.bcgateway.core.model.CurrencyInfo
import co.nilin.opex.bcgateway.core.spi.CurrencyLoader

class CurrencyLoaderImpl: CurrencyLoader {
    override suspend fun fetchCurrencyInfo(symbol: String): CurrencyInfo {
        TODO("Not yet implemented")
    }

    override suspend fun findSymbol(chain: String, address: String?): String {
        TODO("Not yet implemented")
    }

    override suspend fun findImplementationsWithTokenOnChain(chain: String): List<CurrencyImplementation> {
        TODO("Not yet implemented")
    }
}